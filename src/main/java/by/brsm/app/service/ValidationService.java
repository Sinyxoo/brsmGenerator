package by.brsm.app.service;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.model.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Проверка обязательных полей формы перед генерацией документов.
 */
public class ValidationService {

    public List<String> validate(Protocol protocol) {
        List<String> errors = new ArrayList<>();
        if (protocol.getDate() == null) {
            errors.add("Укажите дату протокола.");
        }
        if (protocol.getNumber() <= 0) {
            errors.add("Номер протокола должен быть положительным числом.");
        }
        if (protocol.getChairman() == null) {
            errors.add("Выберите председателя заседания.");
        }
        if (protocol.getMeetingSecretary() == null) {
            errors.add("Выберите секретаря заседания.");
        }
        if (protocol.getAttendees() == null || protocol.getAttendees().isEmpty()) {
            errors.add("Отметьте хотя бы одного присутствующего.");
        }
        if (protocol.getDefaultVotesFor() < 1 || protocol.getDefaultVotesFor() > 50) {
            errors.add("Количество голосов «за» должно быть от 1 до 50.");
        }
        if (protocol.getItems() == null || protocol.getItems().isEmpty()) {
            errors.add("Добавьте хотя бы один вопрос повестки дня.");
        } else {
            int index = 1;
            for (AgendaItem item : protocol.getItems()) {
                errors.addAll(validateItem(item, index));
                index++;
            }
        }
        return errors;
    }

    private List<String> validateItem(AgendaItem item, int index) {
        List<String> errors = new ArrayList<>();
        String prefix = "Вопрос №" + index + ": ";
        if (item.getType() == null) {
            errors.add(prefix + "выберите тип вопроса.");
            return errors;
        }
        if (item.getSupporter() == null) {
            errors.add(prefix + "выберите выступившего.");
        }
        if (item.getSpeaker() == null && item.getType() != AgendaTypeCode.FREEFORM) {
            errors.add(prefix + "не определён докладчик (СЛУШАЛИ).");
        }
        if (item.getVotesFor() < 1) {
            errors.add(prefix + "укажите количество голосов «за».");
        }

        Map<String, Object> f = item.getFields();
        switch (item.getType()) {
            case SETTLEMENT -> {
                if (f.get("facultyId") == null) {
                    errors.add(prefix + "выберите факультет.");
                }
            }
            case PAYMENT -> {
                if (isBlank(f.get("eventName"))) {
                    errors.add(prefix + "укажите название мероприятия.");
                }
                if (isBlank(f.get("amount"))) {
                    errors.add(prefix + "укажите сумму.");
                }
            }
            case COMMITTEE_CHANGE -> {
                @SuppressWarnings("unchecked")
                List<String> excluded = (List<String>) f.getOrDefault("excludedNames", List.of());
                @SuppressWarnings("unchecked")
                List<String> coopted = (List<String>) f.getOrDefault("cooptedNames", List.of());
                if (excluded.isEmpty() && coopted.isEmpty()) {
                    errors.add(prefix + "укажите исключаемых или кооптируемых.");
                }
            }
            case STAFF_FORMATION -> {
                if (isBlank(f.get("staffShortName")) || isBlank(f.get("staffFullName"))) {
                    errors.add(prefix + "укажите название штаба.");
                }
                if (f.get("headcount") == null) {
                    errors.add(prefix + "укажите количество человек.");
                }
            }
            case DOCUMENT_APPROVAL -> {
                if (isBlank(f.get("documentName"))) {
                    errors.add(prefix + "укажите название документа.");
                }
            }
            case ADMISSION -> {
                @SuppressWarnings("unchecked")
                List<String> names = (List<String>) f.getOrDefault("admittedNames", List.of());
                if (names.isEmpty()) {
                    errors.add(prefix + "добавьте принимаемых в ряды.");
                }
            }
            case FREEFORM -> {
                if (isBlank(f.get("customHeader"))) {
                    errors.add(prefix + "укажите заголовок вопроса.");
                }
            }
        }
        return errors;
    }

    private boolean isBlank(Object value) {
        return value == null || String.valueOf(value).isBlank();
    }
}
