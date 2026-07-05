package by.brsm.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Пути рабочих каталогов приложения (данные пользователя, шаблоны, вывод документов).
 */
public final class AppPaths {

    private static final Path BASE = Path.of(System.getProperty("user.home"), ".brsm-generator");
    private static final Path DB = BASE.resolve("application.db");
    private static final Path BACKUP = BASE.resolve("backup");
    private static final Path OUTPUT = BASE.resolve("output");
    private static final Path TEMPLATES = BASE.resolve("templates");
    private static final Path LOG = BASE.resolve("app.log");

    private AppPaths() {
    }

    public static void init() throws IOException {
        Files.createDirectories(BASE);
        Files.createDirectories(BACKUP);
        Files.createDirectories(OUTPUT);
        Files.createDirectories(TEMPLATES);
        copyTemplateIfMissing("protokol_template.docx");
        copyTemplateIfMissing("postanovlenie_template.docx");
    }

    private static void copyTemplateIfMissing(String name) throws IOException {
        Path target = TEMPLATES.resolve(name);
        if (Files.exists(target)) {
            return;
        }
        try (InputStream in = AppPaths.class.getResourceAsStream("/templates/" + name)) {
            if (in != null) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static Path getBaseDir() {
        return BASE;
    }

    public static Path getDatabasePath() {
        return DB;
    }

    public static Path getBackupDir() {
        return BACKUP;
    }

    public static Path getOutputDir() {
        return OUTPUT;
    }

    public static Path getTemplatesDir() {
        return TEMPLATES;
    }

    public static Path getLogPath() {
        return LOG;
    }

    public static Path getProtocolTemplate() {
        return TEMPLATES.resolve("protokol_template.docx");
    }

    public static Path getResolutionTemplate() {
        return TEMPLATES.resolve("postanovlenie_template.docx");
    }
}
