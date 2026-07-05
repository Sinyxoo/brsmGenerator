package by.brsm.app.model;

/**
 * Справочник типов студенческих штабов (для вопросов типа STAFF_FORMATION).
 */
public class StaffType {

    /** Идентификатор записи в базе данных. */
    private int id;

    /** Краткое название, например «трудовых дел». */
    private String shortName;

    /** Полное название, например «Территориального штаба студенческих отрядов». */
    private String fullName;

    public StaffType() {
    }

    public StaffType(int id, String shortName, String fullName) {
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
        return fullName != null && !fullName.isBlank() ? fullName : shortName;
    }
}
