package com.dpardo.strike.ui.data_writer;

import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.domain.SessionManager;
import com.dpardo.strike.domain.UiComboItem;
import com.dpardo.strike.repository.UserRepository;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeAdminController {

    //--- Componentes FXML ---
    @FXML private BorderPane adminBorderPane;
    @FXML private Button paisButton;
    @FXML private Button jugadorButton;
    @FXML private Button equiposButton;
    @FXML private Button partidoButton;
    @FXML private Button ligaButton;
    @FXML private StackPane formContainer;
    @FXML private Button borrarButton;
    @FXML private Button editarButton;

    // Header Components
    @FXML private Button userInfoadminButton;
    @FXML private Tooltip usernameAdminTooltip;
    @FXML private ComboBox<UiComboItem> viewSelectorAdminComboBox;

    //--- Repositorios ---
    private final UserRepository userRepository = new UserRepository();

    //--- Utilidades ---
    private final Map<String, String> uiPathMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadForm("/com/dpardo/strike/ui/data_writer/Form-pais.fxml");
        setupUserInfo();
        setupComboBox();
    }

    //--- Navegación Interna (Menú Lateral) ---
    @FXML
    void handleMenuClick(ActionEvent event) {
        Object source = event.getSource();
        if (source == paisButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-pais.fxml");
        } else if (source == equiposButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-equipo.fxml");
        } else if (source == ligaButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-liga.fxml");
        } else if (source == partidoButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-partido.fxml");
        } else if (source == jugadorButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-jugador.fxml");
        } else if (source == editarButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-editar.fxml");
        } else if (source == borrarButton) {
            loadForm("/com/dpardo/strike/ui/data_writer/Form-borrar.fxml");
        } else {
            clearFormContainer();
        }
    }

    private void loadForm(String fxmlPath) {
        try {
            Node newFormNode = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            if (!formContainer.getChildren().isEmpty()) {
                Node oldFormNode = formContainer.getChildren().get(0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(250), oldFormNode);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    newFormNode.setOpacity(0.0);
                    formContainer.getChildren().setAll(newFormNode);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newFormNode);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } else {
                newFormNode.setOpacity(0.0);
                formContainer.getChildren().add(newFormNode);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newFormNode);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException | NullPointerException e) {
            System.err.println("Error al cargar formulario: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void clearFormContainer() {
        if (!formContainer.getChildren().isEmpty()) {
            formContainer.getChildren().clear();
        }
    }

    //--- Navegación de Roles (Header) ---

    private void setupComboBox() {
        uiPathMap.put("homeBorderPane", "/com/dpardo/strike/ui/read_only/Home-view.fxml");
        uiPathMap.put("adminBorderPane", "/com/dpardo/strike/ui/data_writer/Home-admin.fxml");
        uiPathMap.put("superadminBorderPane", "/com/dpardo/strike/ui/super_user/Home-superadmin.fxml");

        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession != null) {
            int userId = currentSession.userId();
            List<UiComboItem> uis = userRepository.obtenerUisPermitidas(userId);
            viewSelectorAdminComboBox.setItems(FXCollections.observableArrayList(uis));
        }
        viewSelectorAdminComboBox.setOnAction(event -> handleViewSelection());
    }

    @FXML
    private void handleViewSelection() {
        UiComboItem selectedUi = viewSelectorAdminComboBox.getValue();
        if (selectedUi != null) {
            String fxmlPath = uiPathMap.get(selectedUi.codComponente());
            if (fxmlPath != null) {
                openNewWindow(fxmlPath, selectedUi.descripcion());
            }
            Platform.runLater(() -> viewSelectorAdminComboBox.getSelectionModel().clearSelection());
        }
    }

    private void setupUserInfo() {
        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession != null) {
            usernameAdminTooltip.setText("Usuario ID: " + currentSession.userId());
        } else {
            userInfoadminButton.setVisible(false);
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

            // FIX: Tamaño explícito
            Scene scene = new Scene(root, 960, 600);
            stage.setScene(scene);

            stage.setMinWidth(960);
            stage.setMinHeight(600);
            stage.setWidth(960);
            stage.setHeight(600);
            stage.setResizable(false);

            stage.show();
            stage.centerOnScreen();

            // Cerrar ventana actual
            Stage currentStage = (Stage) adminBorderPane.getScene().getWindow();
            currentStage.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}