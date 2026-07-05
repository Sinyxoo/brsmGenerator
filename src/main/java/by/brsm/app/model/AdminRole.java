package by.brsm.app.model;

/**
 * Роль сотрудника в администрации комитета.
 */
public enum AdminRole {

    /** Секретарь комитета (председатель заседания). */
    SECRETARY("Секретарь комитета"),

    /** Заместитель секретаря (секретарь заседания). */
    DEPUTY_SECRETARY("Заместитель секретаря");

    private final String displayName;

    AdminRole(String displayName) {
        this.displayName = displayName;
    }

    /** Отображаемое название роли для интерфейса. */
    public String getDisplayName() {
        return displayName;
    }
}
