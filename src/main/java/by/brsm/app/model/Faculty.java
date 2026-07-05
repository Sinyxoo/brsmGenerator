package by.brsm.app.model;

/**
 * Справочник факультетов БНТУ.
 */
public class Faculty {

    /** Идентификатор записи в базе данных. */
    private int id;

    /** Краткое название, например «ФГДИЭ», «СТФ». */
    private String shortName;

    /** Полное официальное название факультета. */
    private String fullName;

    public Faculty() {
    }

    public Faculty(int id, String shortName, String fullName) {
        this.id = id;
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return shortName;
    }
}
