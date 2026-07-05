package by.brsm.app.ui;

import by.brsm.app.AppContext;
import by.brsm.app.model.AgendaItem;
import by.brsm.app.model.Administrator;
import by.brsm.app.model.Person;
import by.brsm.app.model.Protocol;
import by.brsm.app.service.ProtocolService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Главный контроллер приложения.
 */
public class MainController {

    private AppContext context;
    private ProtocolService protocolService;
    private Protocol currentProtocol;
    private final ObservableList<Protocol> historyList = FXCollections.observableArrayList();

    @FXML private DatePicker datePicker;
    @FXML private Spinner<Integer> protocolNumberSpinner;
    @FXML private ComboBox<Administrator> chairmanCombo;
    @FXML private ComboBox<Administrator> secretaryCombo;
    @FXML private Spinner<Integer> defaultVotesSpinner;
    @FXML private ListView<Person> attendeesList;
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
            setupAttendeesList();
            setupHistoryTable();
            loadHistory();
            onNewProtocol(); // Создаём новый протокол при старте

        } catch (Exception e) {
            showError("Ошибка инициализации", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        protocolNumberSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        defaultVotesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 15));
    }

    private void setupComboboxes() throws java.sql.SQLException {
        // Председатель и секретарь заседания выбираются из общего справочника администрации комитета.
        List<Administrator> administrators = context.getAdministratorDao().findAllActive();
        chairmanCombo.setItems(FXCollections.observableArrayList(administrators));
        secretaryCombo.setItems(FXCollections.observableArrayList(administrators));
        // Administrator.toString() (унаследован от Person) уже возвращает shortName,
        // поэтому отдельный StringConverter не требуется.
    }

    private void setupAttendeesList() throws java.sql.SQLException {
        List<Person> selectable = new ArrayList<>();
        selectable.addAll(context.getAdministratorDao().findAllActive());
        selectable.addAll(context.getFacultySecretaryDao().findAllActive());
        attendeesList.setItems(FXCollections.observableArrayList(selectable));
        attendeesList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
    }

    private void setupHistoryTable() {
        historyNumberColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getNumber())));
        historyDateColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(cell.getValue().getDate())));
        historyItemsColumn.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getItems().size()).asObject());

        historyTable.setItems(historyList);
    }

    private void loadHistory() {
        try {
            historyList.clear();
            historyList.addAll(protocolService.loadHistory());
        } catch (Exception e) {
            showError("Ошибка загрузки истории", e.getMessage());
        }
    }

    @FXML
    private void onNewProtocol() {
        currentProtocol = new Protocol();
        currentProtocol.setDate(LocalDate.now());
        try {
            currentProtocol.setNumber(protocolService.suggestNextProtocolNumber());
        } catch (Exception e) {
            currentProtocol.setNumber(1);
        }

        clearForm();
        agendaCardControllers.clear();
    }

    private void clearForm() {
        datePicker.setValue(currentProtocol.getDate());
        protocolNumberSpinner.getValueFactory().setValue(currentProtocol.getNumber());
        chairmanCombo.getSelectionModel().clearSelection();
        secretaryCombo.getSelectionModel().clearSelection();
        defaultVotesSpinner.getValueFactory().setValue(15);
        attendeesList.getSelectionModel().clearSelection();
        agendaItemsContainer.getChildren().clear();
    }

    @FXML
    private void onAddAgendaItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agenda_item_card.fxml"));
            VBox card = loader.load();

            AgendaItemCardController cardController = loader.getController();
            cardController.setMainController(this);
            cardController.setDefaultVotes(defaultVotesSpinner.getValue());
            agendaCardControllers.add(cardController);
            renumberAgendaCards();

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

            StringBuilder message = new StringBuilder("Протокол успешно создан!\n\nФайл протокола:\n")
                    .append(result.protocolFile());
            if (!result.resolutions().isEmpty()) {
                message.append("\n\nСформированные постановления:");
                for (ProtocolService.GeneratedFile file : result.resolutions()) {
                    message.append("\n").append(file.title()).append(": ").append(file.path());
                }
            }
            showInfo("Генерация завершена", message.toString());

            loadHistory(); // Обновляем историю
            onNewProtocol(); // Начинаем новый

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
            showWarning("Ошибка", "Выберите секретаря заседания");
            return false;
        }
        if (attendeesList.getSelectionModel().getSelectedItems().isEmpty()) {
            showWarning("Ошибка", "Отметьте хотя бы одного присутствующего");
            return false;
        }
        if (agendaCardControllers.isEmpty()) {
            showWarning("Ошибка", "Добавьте хотя бы один вопрос повестки дня");
            return false;
        }
        return true;
    }

    private void collectFormData() {
        currentProtocol.setDate(datePicker.getValue());
        currentProtocol.setNumber(protocolNumberSpinner.getValue());
        currentProtocol.setChairman(chairmanCombo.getValue());
        currentProtocol.setMeetingSecretary(secretaryCombo.getValue());
        currentProtocol.setDefaultVotesFor(defaultVotesSpinner.getValue());

        // Присутствующие
        currentProtocol.getAttendees().clear();
        currentProtocol.getAttendees().addAll(attendeesList.getSelectionModel().getSelectedItems());

        // Повестка дня
        currentProtocol.getItems().clear();
        for (AgendaItemCardController cardCtrl : agendaCardControllers) {
            AgendaItem item = cardCtrl.getAgendaItem();
            if (item != null) {
                currentProtocol.getItems().add(item);
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

            // После закрытия обновляем комбобоксы и список присутствующих
            setupComboboxes();
            setupAttendeesList();
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
            String path = selected.getFilePath();
            if (path == null) {
                showWarning("Файл не найден", "Для этого протокола файл ещё не был сформирован.");
                return;
            }
            File file = new File(path);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showWarning("Файл не найден", "Файл протокола не найден по пути: " + path);
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
        renumberAgendaCards();
    }

    private void renumberAgendaCards() {
        for (int i = 0; i < agendaCardControllers.size(); i++) {
            agendaCardControllers.get(i).setOrder(i + 1);
        }
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
