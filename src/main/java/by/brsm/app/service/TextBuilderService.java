package by.brsm.app.service;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;
import by.brsm.app.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Формирование текстов повестки дня, блоков СЛУШАЛИ/ВЫСТУПИЛИ/ПОСТАНОВИЛИ по спецификации.
 */
public class TextBuilderService {

    private final SumInWordsService sumInWordsService;

    public TextBuilderService(SumInWordsService sumInWordsService) {
        this.sumInWordsService = sumInWordsService;
    }

    /** Заголовок вопроса для повестки дня протокола. */
    public String buildAgendaHeader(AgendaItem item) {
        Map<String, Object> f = item.getFields();
        return switch (item.getType()) {
            case SETTLEMENT -> "О согласовании вопроса о предоставлении обучающимся "
                    + f.get("facultyShortName") + " мест в общежитиях БНТУ.";
            case PAYMENT -> "О выделении денежных средств.";
            case COMMITTEE_CHANGE -> committeeHeader(f);
            case STAFF_FORMATION -> "О внесении изменений в формирование Штаба "
                    + f.get("staffShortName") + " ПО ОО «БРСМ» с правами РК БНТУ.";
            case DOCUMENT_APPROVAL -> "Об утверждении " + f.get("documentName")
                    + " ПО ОО «БРСМ» с правами РК БНТУ" + optionalPeriod(f) + ".";
            case ADMISSION -> "О вступлении в ряды ОО «БРСМ» с правами РК БНТУ.";
            case FREEFORM -> String.valueOf(f.getOrDefault("customHeader", ""));
        };
    }

    /** Заголовок для отдельного файла постановления (может отличаться от протокола). */
    public String buildResolutionHeader(AgendaItem item) {
        Map<String, Object> f = item.getFields();
        return switch (item.getType()) {
            case SETTLEMENT -> """
                    О согласовании вопроса
                    о предоставлении обучающимся
                    мест в общежитиях БНТУ""";
            case PAYMENT -> "О выделении денежных средств";
            case COMMITTEE_CHANGE -> committeeHeader(f).replace(".", "");
            case STAFF_FORMATION -> buildAgendaHeader(item).replace(".", "");
            case DOCUMENT_APPROVAL -> buildAgendaHeader(item).replace(".", "");
            case ADMISSION -> "О  вступлении в ряды \nОО «БРСМ» с правами РК БНТУ";
            case FREEFORM -> String.valueOf(f.getOrDefault("customHeader", ""));
        };
    }

    public String buildSlushaliTopic(AgendaItem item) {
        Map<String, Object> f = item.getFields();
        return switch (item.getType()) {
            case SETTLEMENT -> "о предоставлении обучающимся " + f.get("facultyShortName")
                    + " мест в общежитиях БНТУ";
            case PAYMENT -> "о выделении денежных средств";
            case COMMITTEE_CHANGE -> "о " + StringUtils.lowerFirst(StringUtils.stripTrailingDot(committeeHeader(f)));
            case STAFF_FORMATION -> "о внесении изменений в формирование Штаба "
                    + f.get("staffShortName") + " ПО ОО «БРСМ» с правами РК БНТУ";
            case DOCUMENT_APPROVAL -> "об утверждении " + f.get("documentName") + optionalPeriod(f);
            case ADMISSION -> "о вступлении в ряды ОО «БРСМ» с правами РК БНТУ";
            case FREEFORM -> String.valueOf(f.getOrDefault("customSlushaliTopic", ""));
        };
    }

    public String buildSlushaliText(AgendaItem item, Person speaker) {
        if (item.getType() == AgendaTypeCode.FREEFORM) {
            return String.valueOf(item.getFields().getOrDefault("customSlushaliText", buildSlushaliTopic(item)));
        }
        return speaker.getShortName() + " " + buildSlushaliTopic(item) + ".";
    }

    public String buildVystupiliText(AgendaItem item, Person speaker, Person supporter) {
        if (item.getType() == AgendaTypeCode.FREEFORM) {
            Object custom = item.getFields().get("customVystupiliText");
            if (custom != null && !String.valueOf(custom).isBlank()) {
                return String.valueOf(custom);
            }
        }
        return supporter.getShortName() + " с предложением поддержать выступление "
                + speaker.getShortName() + " " + buildSlushaliTopic(item) + ".";
    }

    /** Пункты блока ПОСТАНОВИЛИ в протоколе. */
    public List<String> buildProtocolResolutionItems(AgendaItem item, Protocol protocol) {
        List<String> items = new ArrayList<>();
        Person speaker = item.getSpeaker();
        items.add("Информацию секретаря ПО ОО «БРСМ» с правами РК БНТУ "
                + speaker.getShortName() + " принять к сведению.");
        items.addAll(buildSpecificItems(item, false));
        items.add("Заместителю секретаря ПО ОО «БРСМ» с правами РК БНТУ ("
                + protocol.getMeetingSecretary().getShortName()
                + ") довести данное Постановление до всех заинтересованных.");
        items.add("Контроль за исполнением данного Постановления возложить на секретаря ПО ОО «БРСМ» с правами РК БНТУ "
                + protocol.getChairman().getShortName() + ".");
        return items;
    }

    /** Пункты блока ПОСТАНОВЛЯЕТ в отдельном файле постановления. */
    public List<String> buildStandaloneResolutionItems(AgendaItem item, Protocol protocol) {
        List<String> items = new ArrayList<>();
        Person speaker = item.getSpeaker();
        String acceptLine = buildAcceptLine(item, speaker, true);
        items.add(acceptLine);
        items.addAll(buildSpecificItems(item, true));
        items.add(buildDeliverLine(item, protocol, true));
        items.add(buildControlLine(item, protocol, true));
        return items;
    }

    public String buildResolutionIntro(AgendaItem item, Protocol protocol, Person speaker) {
        String role = resolveSpeakerRoleGenitive(item, true);
        String topicPhrase = buildResolutionIntroTopic(item);
        return "Заслушав и обсудив информацию " + role + " ПО ОО «БРСМ» с правами РК БНТУ "
                + speaker.getFullNameGenitive() + ", " + topicPhrase + ", Комитет ПО ОО «БРСМ» с правами РК БНТУ ";
    }

    private List<String> buildSpecificItems(AgendaItem item, boolean standalone) {
        Map<String, Object> f = item.getFields();
        return switch (item.getType()) {
            case SETTLEMENT -> List.of("Согласовать вопрос о предоставлении обучающимся "
                    + f.get("facultyShortName") + " мест в общежитиях БНТУ.");
            case PAYMENT -> buildPaymentItems(f, standalone);
            case COMMITTEE_CHANGE -> buildCommitteeItems(f);
            case STAFF_FORMATION -> buildStaffItems(f);
            case DOCUMENT_APPROVAL -> List.of("Утвердить " + f.getOrDefault("documentNameAccusative", f.get("documentName"))
                    + " ПО ОО «БРСМ» с правами РК БНТУ" + optionalPeriod(f) + ".");
            case ADMISSION -> buildAdmissionItems(f, standalone);
            case FREEFORM -> parseMultiline(String.valueOf(f.getOrDefault("customResolutionItems", "")));
        };
    }

    @SuppressWarnings("unchecked")
    private List<String> buildPaymentItems(Map<String, Object> f, boolean standalone) {
        String event = String.valueOf(f.get("eventName"));
        String amount = String.valueOf(f.get("amount"));
        String amountWords = sumInWordsService.toWords(amount);
        String purpose = String.valueOf(f.getOrDefault("purpose", ""));
        String purposeSuffix = purpose.isBlank() ? "." : " " + purpose.trim();
        if (!purposeSuffix.endsWith(".") && !purpose.isBlank()) {
            purposeSuffix += ".";
        }
        if (standalone) {
            return List.of(
                    "Утвердить Положение о проведении мероприятия «" + event + "».",
                    "Утвердить смету на проведение мероприятия «" + event + "».",
                    "Выделить денежные средства в размере " + amount + " (" + amountWords
                            + ") белорусских рублей из периодических членских взносов ПО ОО «БРСМ» с правами РК БНТУ"
                            + purposeSuffix
            );
        }
        return List.of(
                "Утвердить положение о проведении мероприятия «" + event + "».",
                "Утвердить смету расходов на проведение мероприятия «" + event + "».",
                "Выделить денежные средства в размере " + amount + " (" + amountWords
                        + ") белорусских рублей из периодических членских взносов ПО ОО «БРСМ» с правами РК БНТУ"
                        + purposeSuffix
        );
    }

    @SuppressWarnings("unchecked")
    private List<String> buildCommitteeItems(Map<String, Object> f) {
        List<String> result = new ArrayList<>();
        List<String> excluded = (List<String>) f.getOrDefault("excludedNames", List.of());
        List<String> coopted = (List<String>) f.getOrDefault("cooptedNames", List.of());
        if (!excluded.isEmpty()) {
            result.add("Исключить из состава Комитета " + StringUtils.joinComma(excluded) + " (заявление прилагается).");
        }
        if (!coopted.isEmpty()) {
            result.add("Кооптировать в состав комитета " + StringUtils.joinComma(coopted) + ".");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> buildStaffItems(Map<String, Object> f) {
        List<String> result = new ArrayList<>();
        result.add("Внести изменения в формирование " + f.get("staffFullName")
                + " ПО ОО «БРСМ» с правами РК БНТУ в составе " + f.get("headcount") + " человек (приложение 1).");
        List<String> excluded = (List<String>) f.getOrDefault("excludedNames", List.of());
        List<String> added = (List<String>) f.getOrDefault("addedNames", List.of());
        if (!excluded.isEmpty()) {
            result.add("Исключить из состава Штаба " + f.get("staffShortName")
                    + " ПО ОО «БРСМ» с правами РК БНТУ " + StringUtils.joinComma(excluded) + ".");
        }
        if (!added.isEmpty()) {
            result.add("Ввести в состав Штаба " + f.get("staffShortName")
                    + " ПО ОО «БРСМ» с правами РК БНТУ " + StringUtils.joinComma(added) + ".");
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> buildAdmissionItems(Map<String, Object> f, boolean standalone) {
        List<String> names = (List<String>) f.getOrDefault("admittedNames", List.of());
        List<String> result = new ArrayList<>();
        result.add("Принять в ряды Общественного объединения «Белорусский республиканский союз молодежи» следующих:");
        if (standalone) {
            result.addAll(names);
        } else {
            result.addAll(names);
        }
        return result;
    }

    private String buildAcceptLine(AgendaItem item, Person speaker, boolean standalone) {
        String role = standalone && usesMemberRole(item) ? "члена комитета" : "секретаря";
        if (standalone && item.getType() == AgendaTypeCode.PAYMENT) {
            return "1. Информацию секретаря ПО ОО «БРСМ» с правами РК БНТУ "
                    + speaker.getShortName() + " принять к сведению.";
        }
        return "Информацию " + role + " ПО ОО «БРСМ» с правами РК БНТУ "
                + speaker.getFullNameGenitive() + " принять к сведению.";
    }

    private String buildDeliverLine(AgendaItem item, Protocol protocol, boolean standalone) {
        if (standalone && item.getType() == AgendaTypeCode.SETTLEMENT) {
            return "Секретарю заседания комитета ПО ОО «БРСМ» с правами РК БНТУ ("
                    + protocol.getMeetingSecretary().getShortName()
                    + ") довести данное Постановление до всех заинтересованных.";
        }
        if (standalone && (item.getType() == AgendaTypeCode.ADMISSION
                || item.getType() == AgendaTypeCode.DOCUMENT_APPROVAL)) {
            return "Секретарю заседания комитета ПО ОО «БРСМ» с правами РК БНТУ ("
                    + protocol.getMeetingSecretary().getShortName()
                    + ") довести данное Постановление до всех заинтересованных.";
        }
        return "Заместителю секретаря ПО ОО «БРСМ» с правами РК БНТУ ("
                + protocol.getMeetingSecretary().getShortName()
                + ") довести данное Постановление до всех заинтересованных.";
    }

    private String buildControlLine(AgendaItem item, Protocol protocol, boolean standalone) {
        if (standalone && (item.getType() == AgendaTypeCode.SETTLEMENT || item.getType() == AgendaTypeCode.ADMISSION)) {
            return "Контроль за исполнением данного Постановления возложить на председателя заседания Комитета ПО ОО «БРСМ» с правами РК БНТУ "
                    + protocol.getChairman().getShortName() + ".";
        }
        return "Контроль за исполнением данного Постановления возложить на секретаря ПО ОО «БРСМ» с правами РК БНТУ "
                + protocol.getChairman().getShortName() + ".";
    }

    private String resolveSpeakerRoleGenitive(AgendaItem item, boolean standalone) {
        if (standalone && usesMemberRole(item)) {
            return "члена комитета первичной организации Общественного объединения «Белорусский республиканский союз молодежи» с правами районного комитета Белорусского национального технического университета (далее – ПО ОО «БРСМ» с правами РК БНТУ)";
        }
        if (standalone && item.getType() == AgendaTypeCode.PAYMENT) {
            return "секретаря первичной организации Общественного объединения «Белорусский республиканский союз молодежи» с правами районного комитета Белорусского национального технического университета";
        }
        if (standalone && (item.getType() == AgendaTypeCode.COMMITTEE_CHANGE
                || item.getType() == AgendaTypeCode.STAFF_FORMATION
                || item.getType() == AgendaTypeCode.DOCUMENT_APPROVAL)) {
            return "секретаря Комитета первичной организации Общественного объединения «Белорусский республиканский союз молодежи» с правами районного комитета Белорусского национального технического университета (далее – ПО ОО «БРСМ» с правами РК БНТУ)";
        }
        return "секретаря первичной организации Общественного объединения «Белорусский республиканский союз молодежи» с правами районного комитета Белорусского национального технического университета (далее – ПО ОО «БРСМ» с правами РК БНТУ)";
    }

    private String buildResolutionIntroTopic(AgendaItem item) {
        return switch (item.getType()) {
            case SETTLEMENT -> "о согласовании вопроса о предоставлении обучающимся мест в общежитиях БНТУ";
            case PAYMENT -> "об организации и проведении мероприятия «" + item.getFields().get("eventName") + "»";
            case COMMITTEE_CHANGE -> "об " + StringUtils.lowerFirst(StringUtils.stripTrailingDot(committeeHeader(item.getFields())));
            case STAFF_FORMATION -> StringUtils.stripTrailingDot(buildSlushaliTopic(item));
            case DOCUMENT_APPROVAL -> StringUtils.stripTrailingDot(buildSlushaliTopic(item));
            case ADMISSION -> "о согласовании вопроса о  вступлении в ряды ОО «БРСМ» с правами РК БНТУ";
            case FREEFORM -> String.valueOf(item.getFields().getOrDefault("customIntroTopic", buildSlushaliTopic(item)));
        };
    }

    private boolean usesMemberRole(AgendaItem item) {
        return item.getType() == AgendaTypeCode.SETTLEMENT || item.getType() == AgendaTypeCode.ADMISSION;
    }

    private String committeeHeader(Map<String, Object> f) {
        return switch (String.valueOf(f.getOrDefault("headerVariant", "A"))) {
            case "B" -> "Об изменении состава комитета ОО «БРСМ» с правами РК БНТУ.";
            case "C" -> "О составе комитета в ПО ОО «БРСМ» с правами РК БНТУ.";
            default -> "О кадровых вопросах в ПО ОО «БРСМ» с правами РК БНТУ.";
        };
    }

    private String optionalPeriod(Map<String, Object> f) {
        Object period = f.get("period");
        return period == null || String.valueOf(period).isBlank() ? "" : " " + period;
    }

    private List<String> parseMultiline(String text) {
        if (StringUtils.isBlank(text)) {
            return List.of();
        }
        return List.of(text.split("\\R"));
    }
}
