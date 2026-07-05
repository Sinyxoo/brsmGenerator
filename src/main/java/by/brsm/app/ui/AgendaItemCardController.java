package by.brsm.app.ui;

import by.brsm.app.AppContext;
import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import by.brsm.app.model.Faculty;
import by.brsm.app.model.Person;
import by.brsm.app.model.StaffType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер карточки одного вопроса повестки дня.
 *
 * <p>В зависимости от выбранного {@link AgendaTypeCode} строит свой набор
 * дополнительных полей (см. {@link #buildFieldsForType(AgendaTypeCode)}) и
 * собирает из них {@link AgendaItem#getFields()} для дальнейшей передачи
 * в {@code TextBuilderService}.</p>
 */
public class AgendaItemCardController {

    private MainController mainController;

    @FXML private VBox root;
    @FXML private Label orderLabel;
    @FXML private ComboBox<AgendaTypeCode> typeCombo;
    @FXML private GridPane dynamicFields;
    @FXML private Spinner<Integer> votesSpinner;
    @FXML private CheckBox resolutionCheckBox;
    @FXML private ComboBox<Person> speakerCombo;
    @FXML private ComboBox<Person> supporterCombo;

    /** Динамические контролы текущего типа вопроса, ключ — имя поля в AgendaItem.fields. */
    private final Map<String, Control> dynamicControls = new LinkedHashMap<>();

    private List<Faculty> faculties = List.of();
    private List<StaffType> staffTypes = List.of();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadReferenceData();

        votesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 17));

        typeCombo.setItems(FXCollections.observableArrayList(AgendaTypeCode.values()));
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(AgendaTypeCode code) {
                return code != null ? code.getDisplayName() : "";
            }

            @Override
            public AgendaTypeCode fromString(String string) {
                return null;
            }
        });

        List<Person> people = allSelectablePeople();
        speakerCombo.setItems(FXCollections.observableArrayList(people));
        supporterCombo.setItems(FXCollections.observableArrayList(people));

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            buildFieldsForType(newVal);
            resolutionCheckBox.setSelected(newVal == null || newVal.isResolutionRequiredByDefault());
        });
    }

    /** Устанавливает порядковый номер вопроса (вызывается из MainController). */
    public void setOrder(int index) {
        orderLabel.setText(index + ".");
    }

    /** Значение голосов «за» по умолчанию (из общего протокола), если пользователь ещё не менял поле вручную. */
    public void setDefaultVotes(int votes) {
        votesSpinner.getValueFactory().setValue(votes);
    }

    private void loadReferenceData() {
        try {
            AppContext context = AppContext.getInstance();
            faculties = context.getFacultyDao().findAll();
            staffTypes = context.getStaffTypeDao().findAll();
        } catch (Exception e) {
            faculties = List.of();
            staffTypes = List.of();
        }
    }

    private List<Person> allSelectablePeople() {
        List<Person> people = new ArrayList<>();
        try {
            AppContext context = AppContext.getInstance();
            people.addAll(context.getAdministratorDao().findAllActive());
            people.addAll(context.getFacultySecretaryDao().findAllActive());
        } catch (Exception ignored) {
            // Справочники недоступны — оставляем список пустым, пользователь сможет
            // выбрать докладчика/выступившего позже либо положиться на авто-подбор.
        }
        return people;
    }

    /**
     * Перестраивает набор дополнительных полей под конкретный тип вопроса.
     * Ключи полей соответствуют тем, что читает {@code TextBuilderService} и {@code ValidationService}.
     */
    private void buildFieldsForType(AgendaTypeCode type) {
        dynamicFields.getChildren().clear();
        dynamicControls.clear();
        if (type == null) {
            return;
        }

        int row = 0;
        switch (type) {
            case SETTLEMENT -> {
                row = addComboRow(row, "Факультет:", "facultyId", faculties);
            }
            case PAYMENT -> {
                row = addTextRow(row, "Название мероприятия:", "eventName");
                row = addTextRow(row, "Сумма (руб.):", "amount");
                row = addTextRow(row, "Назначение платежа (необязательно):", "purpose");
            }
            case COMMITTEE_CHANGE -> {
                row = addTextAreaRow(row, "Исключаемые из состава (по одному в строке):", "excludedNames");
                row = addTextAreaRow(row, "Кооптируемые в состав (по одному в строке):", "cooptedNames");
            }
            case STAFF_FORMATION -> {
                row = addStaffTypeComboRow(row, "Штаб:", "staffShortName", "staffFullName");
                row = addTextRow(row, "Численность штаба (человек):", "headcount");
                row = addTextAreaRow(row, "Исключаемые из штаба (по одному в строке):", "excludedNames");
                row = addTextAreaRow(row, "Вводимые в штаб (по одному в строке):", "addedNames");
            }
            case DOCUMENT_APPROVAL -> {
                row = addTextRow(row, "Название документа:", "documentName");
                row = addTextRow(row, "Название документа (вин. падеж, необязательно):", "documentNameAccusative");
                row = addTextRow(row, "Период действия (необязательно):", "period");
            }
            case ADMISSION -> {
                row = addTextAreaRow(row, "Принимаемые в ряды (по одному в строке):", "admittedNames");
            }
            case FREEFORM -> {
                row = addTextRow(row, "Заголовок вопроса:", "customHeader");
                row = addTextRow(row, "Тема «СЛУШАЛИ» (необязательно):", "customSlushaliTopic");
                row = addTextAreaRow(row, "Пункты «ПОСТАНОВИЛИ» (по одному в строке):", "customResolutionItems");
            }
        }
    }

    private int addTextRow(int row, String label, String key) {
        TextField field = new TextField();
        dynamicFields.addRow(row, new Label(label), field);
        dynamicControls.put(key, field);
        return row + 1;
    }

    private int addTextAreaRow(int row, String label, String key) {
        TextArea area = new TextArea();
        area.setPrefRowCount(3);
        dynamicFields.addRow(row, new Label(label), area);
        dynamicControls.put(key, area);
        return row + 1;
    }

    private int addComboRow(int row, String label, String key, List<Faculty> items) {
        ComboBox<Faculty> combo = new ComboBox<>(FXCollections.observableArrayList(items));
        dynamicFields.addRow(row, new Label(label), combo);
        dynamicControls.put(key, combo);
        return row + 1;
    }

    private int addStaffTypeComboRow(int row, String label, String shortKey, String fullKey) {
        ComboBox<StaffType> combo = new ComboBox<>(FXCollections.observableArrayList(staffTypes));
        dynamicFields.addRow(row, new Label(label), combo);
        // Один комбобокс отдаёт сразу два поля (short/full name) — сохраняем под обоими ключами
        // при сборе AgendaItem, см. collectDynamicFields().
        dynamicControls.put(shortKey, combo);
        dynamicControls.put(fullKey, combo);
        return row + 1;
    }

    @FXML
    private void onRemove() {
        if (mainController != null) {
            mainController.removeAgendaCard(root, this);
        }
    }

    /**
     * Собирает данные из карточки в объект {@link AgendaItem}.
     * Возвращает {@code null}, если тип вопроса не выбран (пустая карточка — пропускаем).
     */
    public AgendaItem getAgendaItem() {
        AgendaTypeCode type = typeCombo.getValue();
        if (type == null) {
            return null;
        }

        AgendaItem item = new AgendaItem();
        item.setType(type);
        item.setFields(collectDynamicFields());
        item.setSpeaker(speakerCombo.getValue());
        item.setSupporter(supporterCombo.getValue());
        item.setVotesFor(votesSpinner.getValue() != null ? votesSpinner.getValue() : 0);
        item.setRequiresResolution(resolutionCheckBox.isSelected());
        return item;
    }

    private Map<String, Object> collectDynamicFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (Map.Entry<String, Control> entry : dynamicControls.entrySet()) {
            String key = entry.getKey();
            Control control = entry.getValue();

            if (control instanceof TextField tf) {
                fields.put(key, tf.getText() == null ? "" : tf.getText().trim());
            } else if (control instanceof TextArea ta) {
                fields.put(key, parseLines(ta.getText()));
            } else if (control instanceof ComboBox<?> combo) {
                Object value = combo.getValue();
                if (value instanceof Faculty faculty) {
                    fields.put(key, faculty.getId());
                    fields.put("facultyShortName", faculty.getShortName());
                } else if (value instanceof StaffType staffType) {
                    fields.put("staffShortName", staffType.getShortName());
                    fields.put("staffFullName", staffType.getFullName());
                }
            }
        }
        return fields;
    }

    private List<String> parseLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    public VBox getRoot() {
        return root;
    }
}
