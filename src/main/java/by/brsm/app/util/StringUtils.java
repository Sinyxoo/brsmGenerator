package by.brsm.app.util;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Вспомогательные операции со строками.
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static String joinComma(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(", "));
    }

    /** Первая буква строчная — для темы «СЛУШАЛИ». */
    public static String lowerFirst(String text) {
        if (isBlank(text)) {
            return "";
        }
        if (text.length() == 1) {
            return text.toLowerCase();
        }
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

    /** Убирает завершающую точку. */
    public static String stripTrailingDot(String text) {
        if (isBlank(text)) {
            return "";
        }
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }
}
