package by.brsm.app.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Форматирование дат для документов и интерфейса.
 */
public final class DateUtils {

    public static final DateTimeFormatter DOCUMENT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DateUtils() {
    }

    public static String formatDocument(LocalDate date) {
        return date != null ? date.format(DOCUMENT) : "";
    }
}
