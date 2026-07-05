package by.brsm.app.model;

/**
 * Ссылка на запись справочника для хранения в БД (например, faculty_secretary:12).
 */
public record PersonRef(String kind, int id) {

    public static PersonRef parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            return null;
        }
        return new PersonRef(parts[0], Integer.parseInt(parts[1]));
    }

    public static String encode(String kind, int id) {
        return kind + ":" + id;
    }

    @Override
    public String toString() {
        return kind + ":" + id;
    }
}
