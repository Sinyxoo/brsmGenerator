package by.brsm.app.service;

import by.brsm.app.util.AppPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Резервное копирование базы данных при запуске приложения.
 */
public class BackupService {

    private static final int MAX_BACKUPS = 30;
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public void backupDatabaseIfExists() throws IOException {
        Path db = AppPaths.getDatabasePath();
        if (!Files.exists(db)) {
            return;
        }
        Path backupDir = AppPaths.getBackupDir();
        Files.createDirectories(backupDir);
        String name = "application_" + LocalDateTime.now().format(FORMAT) + ".db";
        Files.copy(db, backupDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        cleanupOldBackups(backupDir);
    }

    private void cleanupOldBackups(Path backupDir) throws IOException {
        try (Stream<Path> files = Files.list(backupDir)) {
            files.filter(p -> p.getFileName().toString().startsWith("application_"))
                    .sorted(Comparator.comparing(Path::getFileName).reversed())
                    .skip(MAX_BACKUPS)
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}
