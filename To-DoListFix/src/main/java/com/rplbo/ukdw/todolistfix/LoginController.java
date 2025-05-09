package com.rplbo.ukdw.todolistfix;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label registerLabel;

    @FXML
    public void initialize() {

        registerLabel.setOnMouseClicked(event -> navigateToRegister());
    }

    private void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/id/ac/ukdw/www/rplbo/terkecil/login_page/Register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Could not navigate to register page: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
