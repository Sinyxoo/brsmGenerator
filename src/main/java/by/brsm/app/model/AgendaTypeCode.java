package by.brsm.app.model;

/**
 * Код типа вопроса повестки дня (7 предустановленных категорий по ТЗ).
 */
public enum AgendaTypeCode {

    SETTLEMENT("Заселение"),
    PAYMENT("Проплаты / выделение средств"),
    COMMITTEE_CHANGE("Кадровые изменения в составе комитета"),
    STAFF_FORMATION("Формирование студенческого штаба"),
    DOCUMENT_APPROVAL("Утверждение документа"),
    ADMISSION("Приём в ряды"),
    FREEFORM("Произвольный / Снятие");

    private final String displayName;

    AgendaTypeCode(String displayName) {
        this.displayName = displayName;
    }

    /** Отображаемое название типа для выпадающего списка. */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Возвращает значение по умолчанию флажка «Формировать Постановление».
     * Для FREEFORM — включён, но пользователь может отключить.
     */
    public boolean isResolutionRequiredByDefault() {
        return true;
    }
}
