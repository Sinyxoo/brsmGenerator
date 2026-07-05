package by.brsm.app.ui;

import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.AgendaTypeCode;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Контроллер карточки одного вопроса повестки дня
 */
public class AgendaItemCardController {

    private MainController mainController;

    @FXML private VBox root;                    // Корневой контейнер карточки
    @FXML private ComboBox<AgendaTypeCode> typeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea decisionArea;        // Решение по вопросу (если нужно)

    /**
     * Устанавливаем ссылку на главный контроллер
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        initializeCombobox();
    }

    @FXML
    public void initialize() {
        // Можно добавить дополнительные настройки при загрузке FXML
    }

    private void initializeCombobox() {
        if (typeCombo != null) {
            // Заполняем типы вопросов из enum
            typeCombo.getItems().addAll(AgendaTypeCode.values());

            // Устанавливаем конвертер для красивого отображения
            typeCombo.setConverter(new javafx.util.StringConverter<AgendaTypeCode>() {
                @Override
                public String toString(AgendaTypeCode code) {
                    return code != null ? code.getDisplayName() : "";
                }

                @Override
                public AgendaTypeCode fromString(String string) {
                    return null; // не требуется
                }
            });
        }
    }

    /**
     * Удаление текущей карточки
     */
    @FXML
    private void onDelete() {
        if (mainController != null) {
            mainController.removeAgendaCard(root, this);
        }
    }

    /**
     * Собираем данные из карточки в объект AgendaItem
     */
    public AgendaItem getAgendaItem() {
        if (typeCombo.getValue() == null && descriptionArea.getText().trim().isEmpty()) {
            return null; // Пустая карточка — пропускаем
        }

        AgendaItem item = new AgendaItem();
        item.setType(typeCombo.getValue());
        item.setDescription(descriptionArea.getText().trim());

        if (decisionArea != null) {
            item.setDecision(decisionArea.getText().trim());
        }

        return item;
    }

    /**
     * Заполнение карточки при редактировании (если потребуется в будущем)
     */
    public void setAgendaItem(AgendaItem item) {
        if (item == null) return;

        typeCombo.setValue(item.getType());
        descriptionArea.setText(item.getDescription());
        if (decisionArea != null) {
            decisionArea.setText(item.getDecision() != null ? item.getDecision() : "");
        }
    }

    // Геттеры (по необходимости)
    public VBox getRoot() {
        return root;
    }
}