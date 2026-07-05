package by.brsm.app.model;

/**
 * Базовая сущность «человек» для справочников секретарей факультетов и администрации.
 */
public class Person {

    /** Идентификатор записи в базе данных. */
    private int id;

    /** Полное ФИО в родительном падеже, например «Позневича Кирилла Юрьевича». */
    private String fullNameGenitive;

    /** Краткая форма ФИО, например «Позневич К.Ю.». */
    private String shortName;

    /** Признак активности записи (неактивные скрываются из списков выбора). */
    private boolean active = true;

    public Person() {
    }

    public Person(int id, String fullNameGenitive, String shortName, boolean active) {
        this.id = id;
        this.fullNameGenitive = fullNameGenitive;
        this.shortName = shortName;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullNameGenitive() {
        return fullNameGenitive;
    }

    public void setFullNameGenitive(String fullNameGenitive) {
        this.fullNameGenitive = fullNameGenitive;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return shortName;
    }
}
