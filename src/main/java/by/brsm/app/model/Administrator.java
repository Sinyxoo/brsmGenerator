package by.brsm.app.model;

/**
 * Сотрудник администрации комитета (секретарь или заместитель).
 */
public class Administrator extends Person {

    /** Роль в администрации комитета. */
    private AdminRole role;

    public Administrator() {
    }

    public Administrator(int id, String fullNameGenitive, String shortName,
                         boolean active, AdminRole role) {
        super(id, fullNameGenitive, shortName, active);
        this.role = role;
    }

    public AdminRole getRole() {
        return role;
    }

    public void setRole(AdminRole role) {
        this.role = role;
    }
}
