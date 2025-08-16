package ru.kuramshindev.springaiexample.llm.tool;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

public class ToolUtils {

    /**
     * Исключаем служебные каталоги на любом уровне вложенности.
     */
    public static final Set<String> EXCLUDED_DIRS = Set.of(
            ".git", ".hg", ".svn",
            ".gradle", ".mvn", ".idea", ".vscode",
            "node_modules", "target", "build", "out", "dist",
            ".venv", "venv", "__pycache__", ".next", ".nuxt",
            ".svelte-kit", ".angular", ".terraform", ".tox", ".mypy_cache"
    );

    /**
     * Переводим glob в regex для сопоставления по нормализованным путям.
     */
    public static Pattern globToRegex(String glob) {
        // Поддержка **, *, ?, {a,b}
        StringBuilder sb = new StringBuilder();
        boolean inGroup = false;
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '\\', '.', '(', ')', '+', '|', '^', '$', '@', '%' -> sb.append('\\').append(c);
                case '?' -> sb.append("[^/]");
                case '*' -> {
                    boolean isDouble = (i + 1 < glob.length() && glob.charAt(i + 1) == '*');
                    if (isDouble) {
                        sb.append(".*");
                        i++;
                    } else {
                        sb.append("[^/]*");
                    }
                }
                case '{' -> {
                    sb.append('(');
                    inGroup = true;
                }
                case '}' -> {
                    sb.append(')');
                    inGroup = false;
                }
                case ',' -> sb.append(inGroup ? '|' : ',');
                default -> sb.append(c);
            }
        }
        return Pattern.compile("^" + sb + "$");
    }

    /**
     * Нормализуем относительный путь к виду с '/' для кросс-ОС сопоставления.
     */
    public static String normalize(Path rel) {
        return rel.toString().replace('\\', '/');
    }

    /**
     * Проверяет, находится ли заданный путь внутри одного из исключённых каталогов,
     * указанных в наборе EXCLUDED_DIRS. Проверка производится для всех уровней вложенности
     * относительно базовой директории.
     *
     * @param baseDir Базовая директория, относительно которой производится проверка.
     * @param p       Абсолютный или относительный путь, который требуется проверить.
     * @return {@code true}, если путь находится внутри одного из исключённых каталогов;
     * {@code false}, если путь не пересекается с исключёнными каталогами.
     */
    public static boolean isUnderExcluded(Path baseDir, Path p) {
        Path rel = baseDir.relativize(p);
        for (Path part : rel) {
            String name = part.toString();
            if (EXCLUDED_DIRS.contains(name)) return true;
        }
        return false;
    }

    public static Path resolveSafe(Path baseDir, String relative) {
        Path p = baseDir.resolve(relative).normalize();
        if (!p.startsWith(baseDir)) {
            throw new IllegalArgumentException("Path escapes workspace");
        }
        return p;
    }
}
