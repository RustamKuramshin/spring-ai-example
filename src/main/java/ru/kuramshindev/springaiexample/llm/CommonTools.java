package ru.kuramshindev.springaiexample.llm;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Component
public class CommonTools {

    public String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    public String runCommand(String command) throws Exception {
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
