package ru.kuramshindev.springaiexample.llm.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ru.kuramshindev.springaiexample.llm.tool.ToolUtils.globToRegex;
import static ru.kuramshindev.springaiexample.llm.tool.ToolUtils.isUnderExcluded;
import static ru.kuramshindev.springaiexample.llm.tool.ToolUtils.normalize;
import static ru.kuramshindev.springaiexample.llm.tool.ToolUtils.resolveSafe;

/**
 * Read-only subset of workspace tools for safe agent mode.
 */
@Component
public class WorkspaceReadOnlyTools {

    private final Path baseDir;

    public WorkspaceReadOnlyTools() throws IOException {
        this.baseDir = Path.of("").toRealPath();
    }

    @Tool(description = "Просмотреть файлы в проекте по glob-шаблону (read-only)")
    public List<String> listFiles(
            @ToolParam(description = "Glob-паттерн, напр. **/*.{java,kt,py,js}", required = false) String glob,
            @ToolParam(description = "Максимум возвращаемых путей (по умолчанию 5000)", required = false) Integer maxFiles
    ) throws IOException {

        final int limit = (maxFiles == null || maxFiles <= 0) ? 5000 : Math.min(maxFiles, 50_000);
        final String pattern = (glob == null || glob.isBlank()) ? "**/*" : glob;

        final Pattern regex = globToRegex(pattern);
        final List<String> results = new ArrayList<>(Math.min(limit, 4096));

        try (Stream<Path> s = Files.walk(baseDir)) {
            s.filter(p -> !Files.isDirectory(p))
                    .filter(p -> !isUnderExcluded(baseDir, p))
                    .map(p -> normalize(baseDir.relativize(p)))
                    .filter(rel -> regex.matcher(rel).matches())
                    .sorted()
                    .limit(limit)
                    .forEach(results::add);
        }

        return results;
    }

    @Tool(description = "Прочитать текстовый файл в UTF-8 из рабочей директории (read-only).")
    public String readTextFile(
            @ToolParam(description = "Путь относительно корня проекта") String path,
            @ToolParam(description = "Обрезать до N символов", required = false) Integer maxChars
    ) throws IOException {
        Path p = resolveSafe(baseDir, path);
        String content = Files.readString(p, StandardCharsets.UTF_8);
        if (maxChars != null && maxChars > 0 && content.length() > maxChars) {
            return content.substring(0, maxChars) + "\n... [truncated]";
        }
        return content;
    }
}
