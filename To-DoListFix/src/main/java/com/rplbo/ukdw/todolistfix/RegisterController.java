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

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Label loginLabel;

    @FXML
    public void initialize() {

        loginLabel.setOnMouseClicked(event -> navigateToLogin());
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/id/ac/ukdw/www/rplbo/terkecil/login_page/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Could not navigate to login page: " + e.getMessage());
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
