package com.dpardo.strike.ui.login;

import com.dpardo.strike.domain.SessionInfo;
import com.dpardo.strike.domain.SessionManager;
import com.dpardo.strike.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField tf_username;
    @FXML private PasswordField pf_password;
    @FXML private Button btnLogin;

    private final UserRepository userRepository = new UserRepository();

    @FXML
    public void initialize() {
        if (btnLogin != null) {
            btnLogin.setOnAction(event -> handleLoginButtonAction());
        }
    }

    @FXML
    private void handleLoginButtonAction() {
        String username = tf_username.getText();
        String password = pf_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Entrada Inválida", "Por favor, ingrese su nombre de usuario y contraseña.", Alert.AlertType.WARNING);
            return;
        }

        SessionInfo sessionInfo = userRepository.authenticateAndRegisterSession(username, password);

        if (sessionInfo != null && sessionInfo.roleName() != null) {
            SessionManager.setCurrentSession(sessionInfo);
            // --- CORRECCIÓN DE LOG ---
            System.out.println("Login exitoso. Usuario: " + username + ", Rol: " + sessionInfo.roleName());
            navigateToMainView(sessionInfo.roleName());
        } else {
            showAlert("Error de Autenticación", "Usuario, contraseña o rol incorrectos.", Alert.AlertType.ERROR);
        }
    }

    private void navigateToMainView(String roleName) {
        String fxmlPath;
        String windowTitle = "strike"; // Título unificado

        // --- CORRECCIÓN DE CASE: Usamos guion bajo como en tu BD ---
        switch (roleName.toLowerCase()) {
            case "read_only":
                fxmlPath = "/com/dpardo/strike/ui/read_only/Home-view.fxml";
                break;
            case "data_writer":
                fxmlPath = "/com/dpardo/strike/ui/data_writer/Home-admin.fxml";
                break;
            case "super_user":
                fxmlPath = "/com/dpardo/strike/ui/super_user/Home-superadmin.fxml";
                break;
            default:
                System.err.println("Rol recibido no reconocido: " + roleName);
                showAlert("Error de Rol", "Rol no reconocido: " + roleName, Alert.AlertType.ERROR);
                return;
        }

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Scene scene = new Scene(root, 960, 600);
            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.setScene(scene);

            stage.setMinWidth(960);
            stage.setMinHeight(600);
            stage.setWidth(960);
            stage.setHeight(600);
            stage.setResizable(false);

            stage.show();
            stage.centerOnScreen();

            Stage loginStage = (Stage) btnLogin.getScene().getWindow();
            loginStage.close();

        } catch (IOException | NullPointerException e) {
            showAlert("Error de Carga", "No se pudo cargar la ventana principal para el rol: " + roleName, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}