package com.dpardo.strike.ui.super_user;

import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.domain.SessionManager;
import com.dpardo.strike.domain.SessionViewModel;
import com.dpardo.strike.domain.UiComboItem;
import com.dpardo.strike.repository.SuperAdminRepository;
import com.dpardo.strike.repository.UserRepository;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SuperAdminController {

    //--- Componentes FXML ---
    @FXML private BorderPane superadminBorderPane; // <-- IMPORTANTE
    @FXML private ComboBox<UiComboItem> viewSelectorComboBox;
    @FXML private Button userInfoButton;
    @FXML private Tooltip usernameTooltip;
    @FXML private TableView<SessionViewModel> mainTableView;

    // (Columnas de la tabla)
    @FXML private TableColumn<SessionViewModel, Integer> pidColumn;
    @FXML private TableColumn<SessionViewModel, String> userColumn;
    @FXML private TableColumn<SessionViewModel, String> correoColumn;
    @FXML private TableColumn<SessionViewModel, Timestamp> fecCreacionColumn;
    @FXML private TableColumn<SessionViewModel, String> rolColumn;
    @FXML private TableColumn<SessionViewModel, String> uiColumn;
    @FXML private TableColumn<SessionViewModel, String> direccionIpColumn;
    @FXML private TableColumn<SessionViewModel, Integer> puertoColumn;
    @FXML private TableColumn<SessionViewModel, Timestamp> fecAsignacionColumn;
    @FXML private TableColumn<SessionViewModel, Boolean> activoColumn;

    //--- Repositorios ---
    private final SuperAdminRepository superAdminRepository = new SuperAdminRepository();
    private final UserRepository userRepository = new UserRepository();

    private ScheduledService<ObservableList<SessionViewModel>> sessionUpdateService;
    private final Map<String, String> uiPathMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupUserInfo();
        setupComboBox();
        setupTableView();
        startSessionUpdateService();
    }

    public void stop() {
        if (sessionUpdateService != null) {
            sessionUpdateService.cancel();
        }
    }

    //--- Configuración de Tabla ---
    private void setupTableView() {
        pidColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().pid()).asObject());
        userColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nombreUsuario()));
        correoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().correo()));
        fecCreacionColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().fecCreacionUsuario()));
        rolColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nombreRol()));
        uiColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().codComponenteUi()));
        direccionIpColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().direccionIp()));
        puertoColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().puerto()).asObject());
        fecAsignacionColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().fechaAsignacionRol()));
        activoColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().rolActivo()).asObject());
    }

    private void startSessionUpdateService() {
        sessionUpdateService = new ScheduledService<>() {
            @Override
            protected Task<ObservableList<SessionViewModel>> createTask() {
                return new Task<>() {
                    @Override
                    protected ObservableList<SessionViewModel> call() {
                        return FXCollections.observableArrayList(superAdminRepository.obtenerSesionesActivas());
                    }
                };
            }
        };
        sessionUpdateService.setPeriod(Duration.seconds(3));
        sessionUpdateService.setOnSucceeded(event -> mainTableView.setItems(sessionUpdateService.getValue()));
        sessionUpdateService.start();
    }

    //--- Navegación y Header ---

    private void setupComboBox() {
        uiPathMap.put("homeBorderPane", "/com/dpardo/strike/ui/read_only/Home-view.fxml");
        uiPathMap.put("adminBorderPane", "/com/dpardo/strike/ui/data_writer/Home-admin.fxml");
        uiPathMap.put("superadminBorderPane", "/com/dpardo/strike/ui/super_user/Home-superadmin.fxml");

        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession != null) {
            int userId = currentSession.userId();
            List<UiComboItem> uis = userRepository.obtenerUisPermitidas(userId);
            viewSelectorComboBox.setItems(FXCollections.observableArrayList(uis));
        }
        viewSelectorComboBox.setOnAction(event -> handleViewSelection());
    }

    @FXML
    private void handleViewSelection() {
        UiComboItem selectedUi = viewSelectorComboBox.getValue();
        if (selectedUi != null) {
            String fxmlPath = uiPathMap.get(selectedUi.codComponente());
            if (fxmlPath != null) {
                // No abras la ventana si ya estás en ella
                if (!selectedUi.codComponente().equals("superadminBorderPane")) {
                    openNewWindow(fxmlPath, "strike");
                }
            }
            Platform.runLater(() -> viewSelectorComboBox.getSelectionModel().clearSelection());
        }
    }

    private void setupUserInfo() {
        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession != null) {
            usernameTooltip.setText("Usuario: " + currentSession.userId());
        }
    }

    @FXML
    private void handleUserInfoClick() {
        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession == null) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText("Usuario Actual");
        alert.setContentText("ID: " + currentSession.userId() + "\nRol: " + currentSession.roleName());
        alert.showAndWait();
    }

    private void openNewWindow(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = new Stage();
            stage.setTitle(title);

            Scene scene = new Scene(root, 960, 600);
            stage.setScene(scene);

            stage.setMinWidth(960);
            stage.setMinHeight(600);
            stage.setWidth(960);
            stage.setHeight(600);
            stage.setResizable(false);

            // Animación
            root.setOpacity(0.0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            stage.show();
            stage.centerOnScreen();
            fadeIn.play();

            // --- CORRECCIÓN: Cerrar la ventana actual ---
            Stage currentStage = (Stage) superadminBorderPane.getScene().getWindow();
            currentStage.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}