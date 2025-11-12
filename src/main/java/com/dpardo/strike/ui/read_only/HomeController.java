package com.dpardo.strike.ui.read_only;

import com.dpardo.strike.domain.Pais;
import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.domain.SessionManager;
import com.dpardo.strike.domain.UiComboItem;
import com.dpardo.strike.repository.PaisRepository;
import com.dpardo.strike.repository.UserRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeController {

    // --- Componentes FXML ---
    @FXML private Button jugadoresButton;
    @FXML private Button estadisticasButton;
    @FXML private Button equiposButton;
    @FXML private Button partidosButton;
    @FXML private Button clasificacionButton;
    @FXML private Button resumenButton;
    @FXML private Button ligaButton;
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox paisContenedor;

    // Header Components
    @FXML private ComboBox<UiComboItem> viewHomeComboBox;
    @FXML private Button userinfoHomeButton;
    @FXML private Tooltip usernameAdminTooltip;

    // --- Repositorios ---
    private final PaisRepository paisRepository = new PaisRepository();
    private final UserRepository userRepository = new UserRepository();

    // --- Utilidades ---
    private final Map<String, String> uiPathMap = new HashMap<>();

    @FXML
    public void initialize() {
        cargarPaises();
        setupUserInfo();
        setupComboBox();
    }

    public void cargarPaises() {
        if (paisContenedor != null) {
            paisContenedor.getChildren().clear();
        }
        List<Pais> listaDePaises = paisRepository.obtenerTodosLosPaises();
        for (Pais pais : listaDePaises) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dpardo/strike/ui/read_only/Pais-view.fxml"));
                Node nodoPaisItem = loader.load();
                PaisItemController paisController = loader.getController();

                // Carga segura de imagen
                String rutaImagen = "/images/flags/" + pais.codigo() + ".png";
                if (getClass().getResource(rutaImagen) != null) {
                    Image bandera = new Image(getClass().getResourceAsStream(rutaImagen));
                    paisController.setData(pais.nombre(), bandera);
                    paisContenedor.getChildren().add(nodoPaisItem);
                } else {
                    System.err.println("Imagen no encontrada: " + rutaImagen);
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    // --- Navegación y Header ---

    private void setupComboBox() {
        uiPathMap.put("homeBorderPane", "/com/dpardo/strike/ui/read_only/Home-view.fxml");
        uiPathMap.put("adminBorderPane", "/com/dpardo/strike/ui/data_writer/Home-admin.fxml");
        uiPathMap.put("superadminBorderPane", "/com/dpardo/strike/ui/super_user/Home-superadmin.fxml");

        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession != null) {
            int userId = currentSession.userId();
            List<UiComboItem> uis = userRepository.obtenerUisPermitidas(userId);
            viewHomeComboBox.setItems(FXCollections.observableArrayList(uis));
        }
        viewHomeComboBox.setOnAction(event -> handleViewSelection());
    }

    @FXML
    private void handleViewSelection() {
        UiComboItem selectedUi = viewHomeComboBox.getValue();
        if (selectedUi != null) {
            String fxmlPath = uiPathMap.get(selectedUi.codComponente());
            if (fxmlPath != null) {
                openNewWindow(fxmlPath, selectedUi.descripcion());
            }
            Platform.runLater(() -> viewHomeComboBox.getSelectionModel().clearSelection());
        }
    }

    private void setupUserInfo() {
        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession != null) {
            usernameAdminTooltip.setText("Usuario ID: " + currentSession.userId());
        } else {
            userinfoHomeButton.setVisible(false);
        }
    }

    @FXML
    private void handleUserInfoClick() {
        SessionInfo currentSession = SessionManager.getCurrentSession();
        if (currentSession == null) return;
        mostrarInfoUsuario(currentSession);
    }

    private void mostrarInfoUsuario(SessionInfo s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información del Usuario");
        alert.setHeaderText("Sesión Actual");
        alert.setContentText("Usuario ID: " + s.userId() + "\nRol: " + s.roleName());
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
            Stage currentStage = (Stage) mainBorderPane.getScene().getWindow();
            currentStage.close();

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}