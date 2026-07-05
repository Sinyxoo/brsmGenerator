package by.brsm.app.ui;

import by.brsm.app.AppContext;
import by.brsm.app.model.*;
import by.brsm.app.service.ProtocolService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Главный контроллер приложения
 */
public class MainController {

    private AppContext context;
    private ProtocolService protocolService;
    private Protocol currentProtocol;
    private final ObservableList<Protocol> historyList = FXCollections.observableArrayList();

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> protocolNumberSpinner;
    @FXML private ComboBox<Administrator> chairmanCombo;
    @FXML private ComboBox<FacultySecretary> secretaryCombo;
    @FXML private Spinner<Integer> defaultVotesSpinner;
    @FXML private ListView<Administrator> attendeesList;
    @FXML private VBox agendaItemsContainer;
    @FXML private TableView<Protocol> historyTable;

    @FXML private TableColumn<Protocol, String> historyNumberColumn;
    @FXML private TableColumn<Protocol, String> historyDateColumn;
    @FXML private TableColumn<Protocol, Integer> historyItemsColumn;

    private final List<AgendaItemCardController> agendaCardControllers = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            context = AppContext.getInstance();
            protocolService = context.getProtocolService();

            setupSpinners();
            setupComboboxes();
            setupHistoryTable();
            loadHistory();
            newProtocol(); // Создаём новый протокол при старте

        } catch (Exception e) {
            showError("Ошибка инициализации", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        protocolNumberSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        defaultVotesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 15));
    }

    private void setupComboboxes() {
        // Председатели
        chairmanCombo.setItems(FXCollections.observableArrayList(context.getAdministratorDao().findAll()));
        chairmanCombo.setConverter(new StringConverterForAdministrator()); // Нужно реализовать ниже

        // Секретари
        secretaryCombo.setItems(FXCollections.observableArrayList(context.getFacultySecretaryDao().findAll()));
        secretaryCombo.setConverter(new StringConverterForSecretary());
    }

    private void setupHistoryTable() {
        historyNumberColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumber()));
        historyDateColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getDate().toString()));
        historyItemsColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getAgendaItems().size()).asObject());

        historyTable.setItems(historyList);
    }

    private void loadHistory() {
        historyList.clear();
        historyList.addAll(protocolService.getAllProtocols());
    }

    @FXML
    private void onNewProtocol() {
        currentProtocol = new Protocol();
        currentProtocol.setDate(LocalDate.now());
        currentProtocol.setNumber(String.valueOf(protocolNumberSpinner.getValue()));

        clearForm();
        agendaCardControllers.clear();
    }

    private void clearForm() {
        datePicker.setValue(currentProtocol.getDate());
        protocolNumberSpinner.getValueFactory().setValue(1);
        chairmanCombo.getSelectionModel().clearSelection();
        secretaryCombo.getSelectionModel().clearSelection();
        defaultVotesSpinner.getValueFactory().setValue(15);
        attendeesList.getItems().clear();
        agendaItemsContainer.getChildren().clear();
    }

    @FXML
    private void onAddAgendaItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agenda_item_card.fxml"));
            VBox card = loader.load();

            AgendaItemCardController cardController = loader.getController();
            cardController.setMainController(this);
            agendaCardControllers.add(cardController);

            agendaItemsContainer.getChildren().add(card);
        } catch (Exception e) {
            showError("Ошибка добавления вопроса", e.getMessage());
        }
    }

    @FXML
    private void onGenerate() {
        if (!validateForm()) return;

        collectFormData();

        try {
            ProtocolService.GenerationResult result = protocolService.generateDocuments(currentProtocol);

            showInfo("Генерация завершена",
                    "Протокол успешно создан!\n\n" +
                            "Файлы сохранены в:\n" + result.getProtocolPath());

            loadHistory(); // Обновляем историю
            newProtocol(); // Начинаем новый

        } catch (Exception e) {
            showError("Ошибка генерации документов", e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        if (chairmanCombo.getValue() == null) {
            showWarning("Ошибка", "Выберите председателя");
            return false;
        }
        if (secretaryCombo.getValue() == null) {
            showWarning("Ошибка", "Выберите секретаря");
            return false;
        }
        if (agendaItemsContainer.getChildren().isEmpty()) {
            showWarning("Ошибка", "Добавьте хотя бы один вопрос повестки дня");
            return false;
        }
        return true;
    }

    private void collectFormData() {
        currentProtocol.setDate(datePicker.getValue());
        currentProtocol.setNumber(String.valueOf(protocolNumberSpinner.getValue()));
        currentProtocol.setChairman(chairmanCombo.getValue());
        currentProtocol.setSecretary(secretaryCombo.getValue());

        // Присутствующие
        currentProtocol.getAttendees().clear();
        currentProtocol.getAttendees().addAll(attendeesList.getItems());

        // Повестка дня
        currentProtocol.getAgendaItems().clear();
        for (AgendaItemCardController cardCtrl : agendaCardControllers) {
            AgendaItem item = cardCtrl.getAgendaItem();
            if (item != null) {
                currentProtocol.getAgendaItems().add(item);
            }
        }
    }

    @FXML
    private void onOpenReferences() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reference_editor.fxml"));
            BorderPane root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Редактор справочников");
            stage.setScene(new Scene(root, 900, 600));
            stage.showAndWait();

            // После закрытия обновляем комбобоксы
            setupComboboxes();
        } catch (Exception e) {
            showError("Ошибка открытия справочников", e.getMessage());
        }
    }

    @FXML
    private void onOpenSelectedProtocol() {
        Protocol selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Внимание", "Выберите протокол в истории");
            return;
        }

        try {
            File file = new File(selected.getProtocolPath());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showWarning("Файл не найден", "Файл протокола не найден по пути: " + selected.getProtocolPath());
            }
        } catch (Exception e) {
            showError("Ошибка открытия файла", e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        Stage stage = (Stage) datePicker.getScene().getWindow();
        stage.close();
    }

    // Вспомогательные методы для карточек
    public void removeAgendaCard(VBox card, AgendaItemCardController controller) {
        agendaItemsContainer.getChildren().remove(card);
        agendaCardControllers.remove(controller);
    }

    // Утилиты уведомлений
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}