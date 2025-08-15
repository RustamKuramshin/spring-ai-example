package ru.kuramshindev.springaiexample.llm;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class GradleTools {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);
    private static final int MAX_OUTPUT_CHARS = 200_000; // защита от слишком длинного лога

    private final Path workDir = Path.of("").toAbsolutePath();
    private final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    private List<String> gradleCommandBase() {
        // Строго через wrapper; если его нет — бросаем ошибку
        Path wrapper = workDir.resolve(isWindows ? "gradlew.bat" : "gradlew");
        if (!Files.exists(wrapper)) {
            throw new IllegalStateException("Gradle wrapper не найден в рабочей директории (" + wrapper + ")");
        }
        if (!isWindows) {
            wrapper.toFile().setExecutable(true); // на всякий случай
        }
        List<String> cmd = new ArrayList<>();
        if (isWindows) {
            cmd.add("cmd");
            cmd.add("/c");
            cmd.add(wrapper.toString());
        } else {
            cmd.add(wrapper.toString());
        }
        // Консоль попроще, без демона, по-тише
        cmd.add("--no-daemon");
        cmd.add("--console=plain");
        cmd.add("-q");
        return cmd;
    }

    private String runGradle(Duration timeout, String... tasksAndArgs) throws IOException, InterruptedException {
        List<String> cmd = gradleCommandBase();
        for (String a : tasksAndArgs) cmd.add(a);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process p = pb.start();

        StringBuilder out = new StringBuilder(8192);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (out.length() < MAX_OUTPUT_CHARS) {
                    out.append(line).append('\n');
                }
            }
        }

        boolean finished = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            return "exit=124\nTIMEOUT after " + timeout.toSeconds() + "s\n" + out;
        }
        int code = p.exitValue();
        String result = out.toString();
        if (out.length() >= MAX_OUTPUT_CHARS) {
            result += "\n...[truncated]";
        }
        return "exit=" + code + "\n" + result;
    }

    // ---------- Инструменты (белый список задач) ----------

    @Tool(description = "Запустить unit-тесты Gradle (gradlew test). Возвращает код выхода и лог.")
    public String gradleTest() throws IOException, InterruptedException {
        return runGradle(DEFAULT_TIMEOUT, "test");
    }

    @Tool(description = "Собрать артефакты без тестов (gradlew assemble -x test).")
    public String gradleAssembleSkipTests() throws IOException, InterruptedException {
        return runGradle(DEFAULT_TIMEOUT, "assemble", "-x", "test");
    }

    @Tool(description = "Полная сборка без тестов (gradlew build -x test).")
    public String gradleBuildSkipTests() throws IOException, InterruptedException {
        return runGradle(DEFAULT_TIMEOUT, "build", "-x", "test");
    }

    @Tool(description = "Полная сборка с тестами (gradlew build).")
    public String gradleBuild() throws IOException, InterruptedException {
        return runGradle(DEFAULT_TIMEOUT, "build");
    }

    @Tool(description = "Проверки качества кода (gradlew check). Часто включает тесты и статический анализ.")
    public String gradleCheck() throws IOException, InterruptedException {
        return runGradle(DEFAULT_TIMEOUT, "check");
    }

    @Tool(description = "Очистить сборку (gradlew clean).")
    public String gradleClean() throws IOException, InterruptedException {
        return runGradle(Duration.ofMinutes(3), "clean");
    }

    @Tool(description = "Обновить зависимости кэша (gradlew --refresh-dependencies assemble -x test). Полезно при конфликтах артефактов.")
    public String gradleRefreshDepsAssemble() throws IOException, InterruptedException {
        return runGradle(DEFAULT_TIMEOUT, "--refresh-dependencies", "assemble", "-x", "test");
    }
}
