package by.brsm.app.model;

/**
 * Секретарь первичной организации БРСМ на факультете.
 */
public class FacultySecretary extends Person {

    /** Идентификатор факультета (внешний ключ). */
    private int facultyId;

    /** Объект факультета (заполняется при загрузке из БД). */
    private Faculty faculty;

    public FacultySecretary() {
    }

    public FacultySecretary(int id, String fullNameGenitive, String shortName,
                            boolean active, int facultyId) {
        super(id, fullNameGenitive, shortName, active);
        this.facultyId = facultyId;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(int facultyId) {
        this.facultyId = facultyId;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
        if (faculty != null) {
            this.facultyId = faculty.getId();
        }
    }
}
