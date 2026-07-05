package by.brsm.app.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Один пункт повестки дня протокола.
 */
public class AgendaItem {

    /** Идентификатор записи в базе данных. */
    private int id;

    /** Идентификатор протокола-владельца. */
    private int protocolId;

    /** Порядковый номер вопроса в протоколе (1, 2, 3…). */
    private int orderIndex;

    /** Тип вопроса из перечня AgendaTypeCode. */
    private AgendaTypeCode type;

    /**
     * Специфичные поля по типу вопроса (факультет, сумма, списки ФИО и т.д.).
     * Хранятся в БД как JSON.
     */
    private Map<String, Object> fields = new LinkedHashMap<>();

    /** Докладчик в блоке «СЛУШАЛИ» (определяется правилом по типу или вручную). */
    private Person speaker;

    /** Выступивший в блоке «ВЫСТУПИЛИ» (выбор пользователя). */
    private Person supporter;

    /** Количество голосов «за» по данному вопросу. */
    private int votesFor;

    /** Нужно ли формировать отдельный файл «Постановление». */
    private boolean requiresResolution;

    /** Номер постановления (null, если постановление не формировалось). */
    private Integer resolutionNumber;

    /** Путь к сгенерированному файлу постановления. */
    private String resolutionFilePath;

    public AgendaItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public AgendaTypeCode getType() {
        return type;
    }

    public void setType(AgendaTypeCode type) {
        this.type = type;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields != null ? fields : new LinkedHashMap<>();
    }

    public Person getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Person speaker) {
        this.speaker = speaker;
    }

    public Person getSupporter() {
        return supporter;
    }

    public void setSupporter(Person supporter) {
        this.supporter = supporter;
    }

    public int getVotesFor() {
        return votesFor;
    }

    public void setVotesFor(int votesFor) {
        this.votesFor = votesFor;
    }

    public boolean isRequiresResolution() {
        return requiresResolution;
    }

    public void setRequiresResolution(boolean requiresResolution) {
        this.requiresResolution = requiresResolution;
    }

    public Integer getResolutionNumber() {
        return resolutionNumber;
    }

    public void setResolutionNumber(Integer resolutionNumber) {
        this.resolutionNumber = resolutionNumber;
    }

    public String getResolutionFilePath() {
        return resolutionFilePath;
    }

    public void setResolutionFilePath(String resolutionFilePath) {
        this.resolutionFilePath = resolutionFilePath;
    }
}
