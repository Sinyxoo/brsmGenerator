package by.brsm.app.model;

/**
 * Строка таблицы-приложения к постановлению (ростер штаба).
 */
public class RosterEntry {

    /** Фамилия, имя, отчество. */
    private String fullName;

    /** Место работы/учёбы и должность. */
    private String workplace;

    public RosterEntry() {
    }

    public RosterEntry(String fullName, String workplace) {
        this.fullName = fullName;
        this.workplace = workplace;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }
}
