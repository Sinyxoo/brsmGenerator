package by.brsm.app.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Протокол заседания комитета.
 */
public class Protocol {

    /** Идентификатор записи в базе данных. */
    private int id;

    /** Номер протокола (уникальный, сквозной). */
    private int number;

    /** Дата заседания. */
    private LocalDate date;

    /** Председатель заседания (роль SECRETARY). */
    private Administrator chairman;

    /** Секретарь заседания (роль DEPUTY_SECRETARY). */
    private Administrator meetingSecretary;

    /** Список присутствовавших (секретари факультетов и администрация). */
    private List<Person> attendees = new ArrayList<>();

    /** Общее количество голосов «за» по умолчанию для всех вопросов. */
    private int defaultVotesFor = 17;

    /** Вопросы повестки дня. */
    private List<AgendaItem> items = new ArrayList<>();

    /** Путь к сгенерированному файлу протокола. */
    private String filePath;

    public Protocol() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Administrator getChairman() {
        return chairman;
    }

    public void setChairman(Administrator chairman) {
        this.chairman = chairman;
    }

    public Administrator getMeetingSecretary() {
        return meetingSecretary;
    }

    public void setMeetingSecretary(Administrator meetingSecretary) {
        this.meetingSecretary = meetingSecretary;
    }

    public List<Person> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Person> attendees) {
        this.attendees = attendees != null ? attendees : new ArrayList<>();
    }

    public int getDefaultVotesFor() {
        return defaultVotesFor;
    }

    public void setDefaultVotesFor(int defaultVotesFor) {
        this.defaultVotesFor = defaultVotesFor;
    }

    public List<AgendaItem> getItems() {
        return items;
    }

    public void setItems(List<AgendaItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
