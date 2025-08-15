package ru.kuramshindev.springaiexample.llm.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class MavenTools {

    private String run(String... args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) out.append(line).append('\n');
        }
        int code = p.waitFor();
        return "exit=" + code + "\n" + out;
    }

    @Tool(description = "Запустить unit-тесты через Maven Wrapper (mvnw test). Возвращает код выхода и лог.")
    public String mvnTest() throws IOException, InterruptedException {
        return run("./mvnw", "-q", "test");
    }

    @Tool(description = "Собрать проект без тестов (mvnw -DskipTests package). Возвращает код выхода и лог.")
    public String mvnPackageSkipTests() throws IOException, InterruptedException {
        return run("./mvnw", "-q", "-DskipTests", "package");
    }
}
