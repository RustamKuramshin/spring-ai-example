package ru.kuramshindev.springaiexample.llm.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Component
public class CommonTools {

    @Tool(description = "Вернуть текущее дата/время в часовом поясе текущего контекста (ISO-8601 с зоной).")
    public String getCurrentDateTime() {
        return LocalDateTime.now()
                .atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .toString();
    }

    @Tool(description = """
            Выполнить команду в shell (bash -c) в текущей рабочей директории и вернуть stdout/stderr и код выхода.
            ВНИМАНИЕ: использовать только для разрешённых, безопасных команд.
            """)
    public String runCommand(
            @ToolParam(description = "Полная команда для bash -c") String command
    ) throws Exception {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("bash", "-c", command);

        Process process = builder.start();

        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader =
                     new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            while ((line = errorReader.readLine()) != null) {
                error.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();

            output.append("\nExit Code: ").append(exitCode);
            if (!error.isEmpty()) {
                output.append("\nErrors:\n").append(error);
            }
        }

        return output.toString().trim();
    }
}
