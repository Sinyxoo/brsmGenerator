package by.brsm.app.ui;

import by.brsm.app.AppContext;
import by.brsm.app.model.AdminRole;
import by.brsm.app.model.Administrator;
import by.brsm.app.model.Faculty;
import by.brsm.app.model.FacultySecretary;
import by.brsm.app.model.StaffType;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер экрана «Редактор справочников»: факультеты, секретари факультетов,
 * администрация комитета и типы штабов.
 *
 * <p>Каждая таблица редактируется прямо в ячейках (двойной клик), кнопка
 * «Добавить» создаёт новую пустую строку, «Сохранить» пишет все строки в БД
 * (вставляет новые, обновляет существующие), «Деактивировать» помечает
 * выбранную запись неактивной, не удаляя её физически.</p>
 */
public class ReferenceEditorController {

    private AppContext context;

    @FXML private TableView<Faculty> facultyTable;
    @FXML private TableColumn<Faculty, String> facultyShortColumn;
    @FXML private TableColumn<Faculty, String> facultyFullColumn;

    @FXML private TableView<FacultySecretary> secretaryTable;
    @FXML private TableColumn<FacultySecretary, String> secShortColumn;
    @FXML private TableColumn<FacultySecretary, String> secGenitiveColumn;
    @FXML private TableColumn<FacultySecretary, String> secFacultyColumn;
    @FXML private TableColumn<FacultySecretary, Boolean> secActiveColumn;

    @FXML private TableView<Administrator> adminTable;
    @FXML private TableColumn<Administrator, String> adminShortColumn;
    @FXML private TableColumn<Administrator, String> adminGenitiveColumn;
    @FXML private TableColumn<Administrator, String> adminRoleColumn;
    @FXML private TableColumn<Administrator, Boolean> adminActiveColumn;

    @FXML private TableView<StaffType> staffTable;
    @FXML private TableColumn<StaffType, String> staffShortColumn;
    @FXML private TableColumn<StaffType, String> staffFullColumn;

    /** Список коротких названий факультетов для выпадающего списка в таблице секретарей. */
    private final ObservableList<String> facultyNameOptions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            context = AppContext.getInstance();
            setupFacultyTable();
            setupSecretaryTable();
            setupAdminTable();
            setupStaffTable();
            reloadAll();
        } catch (Exception e) {
            showError("Ошибка инициализации справочников", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Faculties

    private void setupFacultyTable() {
        facultyTable.setEditable(true);

        facultyShortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShortName()));
        facultyShortColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        facultyShortColumn.setOnEditCommit(e -> e.getRowValue().setShortName(e.getNewValue()));

        facultyFullColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        facultyFullColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        facultyFullColumn.setOnEditCommit(e -> e.getRowValue().setFullName(e.getNewValue()));
    }

    @FXML
    private void onAddFaculty() {
        facultyTable.getItems().add(new Faculty(0, "", ""));
    }

    @FXML
    private void onSaveFaculty() {
        try {
            for (Faculty faculty : facultyTable.getItems()) {
                if (faculty.getId() == 0) {
                    context.getFacultyDao().insert(faculty);
                } else {
                    context.getFacultyDao().update(faculty);
                }
            }
            showInfo("Готово", "Список факультетов сохранён.");
            reloadFaculties();
            reloadSecretaries(); // у секретарей отображается название факультета
        } catch (Exception e) {
            showError("Ошибка сохранения факультетов", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Faculty secretaries

    private void setupSecretaryTable() {
        secretaryTable.setEditable(true);

        secShortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShortName()));
        secShortColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        secShortColumn.setOnEditCommit(e -> e.getRowValue().setShortName(e.getNewValue()));

        secGenitiveColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullNameGenitive()));
        secGenitiveColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        secGenitiveColumn.setOnEditCommit(e -> e.getRowValue().setFullNameGenitive(e.getNewValue()));

        secFacultyColumn.setCellValueFactory(c -> {
            Faculty f = c.getValue().getFaculty();
            return new SimpleStringProperty(f != null ? f.getShortName() : "");
        });
        secFacultyColumn.setCellFactory(ComboBoxTableCell.forTableColumn(facultyNameOptions));
        secFacultyColumn.setOnEditCommit(e -> {
            try {
                context.getFacultyDao().findAll().stream()
                        .filter(f -> f.getShortName().equals(e.getNewValue()))
                        .findFirst()
                        .ifPresent(e.getRowValue()::setFaculty);
            } catch (Exception ignored) {
                // справочник факультетов недоступен — оставляем предыдущее значение
            }
        });

        secActiveColumn.setCellValueFactory(c -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(c.getValue().isActive());
            prop.addListener((obs, oldVal, newVal) -> c.getValue().setActive(newVal));
            return prop;
        });
        secActiveColumn.setCellFactory(CheckBoxTableCell.forTableColumn(secActiveColumn));
    }

    @FXML
    private void onAddSecretary() {
        FacultySecretary secretary = new FacultySecretary();
        secretary.setShortName("");
        secretary.setFullNameGenitive("");
        try {
            List<Faculty> faculties = context.getFacultyDao().findAll();
            if (!faculties.isEmpty()) {
                secretary.setFaculty(faculties.get(0));
            }
        } catch (Exception ignored) {
        }
        secretaryTable.getItems().add(secretary);
    }

    @FXML
    private void onSaveSecretary() {
        try {
            for (FacultySecretary secretary : secretaryTable.getItems()) {
                if (secretary.getFaculty() == null) {
                    showWarning("Ошибка", "У секретаря «" + secretary.getShortName() + "» не выбран факультет.");
                    return;
                }
                if (secretary.getId() == 0) {
                    context.getFacultySecretaryDao().insert(secretary);
                } else {
                    context.getFacultySecretaryDao().update(secretary);
                }
            }
            showInfo("Готово", "Список секретарей факультетов сохранён.");
            reloadSecretaries();
        } catch (Exception e) {
            showError("Ошибка сохранения секретарей", e.getMessage());
        }
    }

    @FXML
    private void onDeactivateSecretary() {
        FacultySecretary selected = secretaryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Внимание", "Выберите секретаря в таблице.");
            return;
        }
        try {
            if (selected.getId() != 0) {
                context.getFacultySecretaryDao().deactivate(selected.getId());
            }
            reloadSecretaries();
        } catch (Exception e) {
            showError("Ошибка деактивации", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Administrators

    private void setupAdminTable() {
        adminTable.setEditable(true);

        adminShortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShortName()));
        adminShortColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        adminShortColumn.setOnEditCommit(e -> e.getRowValue().setShortName(e.getNewValue()));

        adminGenitiveColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullNameGenitive()));
        adminGenitiveColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        adminGenitiveColumn.setOnEditCommit(e -> e.getRowValue().setFullNameGenitive(e.getNewValue()));

        adminRoleColumn.setCellValueFactory(c -> {
            AdminRole role = c.getValue().getRole();
            return new SimpleStringProperty(role != null ? role.getDisplayName() : "");
        });
        ObservableList<String> roleNames = FXCollections.observableArrayList(
                AdminRole.SECRETARY.getDisplayName(), AdminRole.DEPUTY_SECRETARY.getDisplayName());
        adminRoleColumn.setCellFactory(ComboBoxTableCell.forTableColumn(roleNames));
        adminRoleColumn.setOnEditCommit(e -> {
            for (AdminRole role : AdminRole.values()) {
                if (role.getDisplayName().equals(e.getNewValue())) {
                    e.getRowValue().setRole(role);
                    break;
                }
            }
        });

        adminActiveColumn.setCellValueFactory(c -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(c.getValue().isActive());
            prop.addListener((obs, oldVal, newVal) -> c.getValue().setActive(newVal));
            return prop;
        });
        adminActiveColumn.setCellFactory(CheckBoxTableCell.forTableColumn(adminActiveColumn));
    }

    @FXML
    private void onAddAdmin() {
        Administrator admin = new Administrator();
        admin.setShortName("");
        admin.setFullNameGenitive("");
        admin.setRole(AdminRole.DEPUTY_SECRETARY);
        adminTable.getItems().add(admin);
    }

    @FXML
    private void onSaveAdmin() {
        try {
            for (Administrator admin : adminTable.getItems()) {
                if (admin.getRole() == null) {
                    showWarning("Ошибка", "У сотрудника «" + admin.getShortName() + "» не выбрана роль.");
                    return;
                }
                if (admin.getId() == 0) {
                    context.getAdministratorDao().insert(admin);
                } else {
                    context.getAdministratorDao().update(admin);
                }
            }
            showInfo("Готово", "Список администрации сохранён.");
            reloadAdmins();
        } catch (Exception e) {
            showError("Ошибка сохранения администрации", e.getMessage());
        }
    }

    @FXML
    private void onDeactivateAdmin() {
        Administrator selected = adminTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Внимание", "Выберите сотрудника в таблице.");
            return;
        }
        try {
            if (selected.getId() != 0) {
                context.getAdministratorDao().deactivate(selected.getId());
            }
            reloadAdmins();
        } catch (Exception e) {
            showError("Ошибка деактивации", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Staff types

    private void setupStaffTable() {
        staffTable.setEditable(true);

        staffShortColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getShortName()));
        staffShortColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        staffShortColumn.setOnEditCommit(e -> e.getRowValue().setShortName(e.getNewValue()));

        staffFullColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        staffFullColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        staffFullColumn.setOnEditCommit(e -> e.getRowValue().setFullName(e.getNewValue()));
    }

    @FXML
    private void onAddStaff() {
        staffTable.getItems().add(new StaffType(0, "", ""));
    }

    @FXML
    private void onSaveStaff() {
        try {
            for (StaffType type : staffTable.getItems()) {
                if (type.getId() == 0) {
                    context.getStaffTypeDao().insert(type);
                } else {
                    context.getStaffTypeDao().update(type);
                }
            }
            showInfo("Готово", "Список штабов сохранён.");
            reloadStaffTypes();
        } catch (Exception e) {
            showError("Ошибка сохранения штабов", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- Common

    @FXML
    private void onClose() {
        Stage stage = (Stage) facultyTable.getScene().getWindow();
        stage.close();
    }

    private void reloadAll() throws Exception {
        reloadFaculties();
        reloadSecretaries();
        reloadAdmins();
        reloadStaffTypes();
    }

    private void reloadFaculties() throws Exception {
        List<Faculty> faculties = context.getFacultyDao().findAll();
        facultyTable.setItems(FXCollections.observableArrayList(faculties));
        facultyNameOptions.setAll(faculties.stream().map(Faculty::getShortName).collect(Collectors.toList()));
    }

    private void reloadSecretaries() throws Exception {
        secretaryTable.setItems(FXCollections.observableArrayList(context.getFacultySecretaryDao().findAll(true)));
    }

    private void reloadAdmins() throws Exception {
        adminTable.setItems(FXCollections.observableArrayList(context.getAdministratorDao().findAll(true)));
    }

    private void reloadStaffTypes() throws Exception {
        staffTable.setItems(FXCollections.observableArrayList(context.getStaffTypeDao().findAll()));
    }

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
