package ru.kuramshindev.springaiexample.llm;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@Component
public class WorkspaceTools {

    private final Path baseDir;

    public WorkspaceTools() throws IOException {
        this.baseDir = Path.of("").toRealPath();
    }

    private Path resolveSafe(String relative) {
        Path p = baseDir.resolve(relative).normalize();
        if (!p.startsWith(baseDir)) {
            throw new IllegalArgumentException("Path escapes workspace");
        }
        return p;
    }

    @Tool(description = """
        Просмотреть файлы в проекте.
        Поддерживает glob-паттерн (например, **/*.java), исключает .git/target.
        """)
    public List<String> listFiles(
            @ToolParam(description = "Glob-паттерн, например **/*.java", required = false) String glob,
            @ToolParam(description = "Максимальная глубина поиска", required = false) Integer maxDepth
    ) throws IOException {
        String pattern = (glob == null || glob.isBlank()) ? "**/*" : glob;
        int depth = (maxDepth == null ? 8 : Math.max(1, Math.min(maxDepth, 20)));
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

        try (Stream<Path> s = Files.walk(baseDir, depth)) {
            return s.filter(p -> !Files.isDirectory(p))
                    .filter(p -> {
                        String rel = baseDir.relativize(p).toString();
                        return !rel.startsWith(".git") && !rel.startsWith("target");
                    })
                    .map(p -> baseDir.relativize(p).toString())
                    .filter(rel -> matcher.matches(Path.of(rel)))
                    .sorted()
                    .toList();
        }
    }

    @Tool(description = "Прочитать текстовый файл в UTF-8 из рабочей директории.")
    public String readTextFile(
            @ToolParam(description = "Путь относительно корня проекта") String path,
            @ToolParam(description = "Обрезать до N символов", required = false) Integer maxChars
    ) throws IOException {
        Path p = resolveSafe(path);
        String content = Files.readString(p, StandardCharsets.UTF_8);
        if (maxChars != null && maxChars > 0 && content.length() > maxChars) {
            return content.substring(0, maxChars) + "\n... [truncated]";
        }
        return content;
    }

    @Tool(description = """
        Создать/перезаписать текстовый файл в UTF-8. Если родительская папка отсутствует — создаст её.
        Возвращает краткое резюме о записи.
        """)
    public String writeTextFile(
            @ToolParam(description = "Путь относительно корня проекта") String path,
            @ToolParam(description = "Полное содержимое файла (UTF-8)") String content,
            @ToolParam(description = "Если false и файл уже есть — бросит ошибку") boolean overwrite
    ) throws IOException {
        Path p = resolveSafe(path);
        Files.createDirectories(p.getParent());
        if (Files.exists(p) && !overwrite) {
            throw new IllegalStateException("File exists: " + path);
        }
        Files.writeString(p, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return "Wrote " + path + " (" + content.length() + " chars)";
    }
}
