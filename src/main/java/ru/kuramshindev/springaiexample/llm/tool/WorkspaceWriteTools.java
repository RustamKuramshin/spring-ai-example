package ru.kuramshindev.springaiexample.llm.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static ru.kuramshindev.springaiexample.llm.tool.ToolUtils.resolveSafe;

/**
 * Write-capable subset of workspace tools for agent Code mode.
 */
@Component
public class WorkspaceWriteTools {

    private final Path baseDir;

    public WorkspaceWriteTools() throws IOException {
        this.baseDir = Path.of("").toRealPath();
    }

    @Tool(description = "Создать/перезаписать текстовый файл в UTF-8. Если родительская папка отсутствует — создаст её.")
    public String writeTextFile(
            @ToolParam(description = "Путь относительно корня проекта") String path,
            @ToolParam(description = "Полное содержимое файла (UTF-8)") String content,
            @ToolParam(description = "Если false и файл уже есть — бросит ошибку") boolean overwrite
    ) throws IOException {
        Path p = resolveSafe(baseDir, path);
        Files.createDirectories(p.getParent());
        if (Files.exists(p) && !overwrite) {
            throw new IllegalStateException("File exists: " + path);
        }
        Files.writeString(p, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return "Wrote " + path + " (" + content.length() + " chars)";
    }
}
