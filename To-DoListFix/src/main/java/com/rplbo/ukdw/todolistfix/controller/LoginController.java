package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import com.rplbo.ukdw.todolistfix.dao.UserDAO;
import com.rplbo.ukdw.todolistfix.dao.UserDAOManager;
import com.rplbo.ukdw.todolistfix.model.User;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

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
    private Label errorLabel;
    @FXML
    public void initialize() {

        registerLabel.setOnMouseClicked(event -> navigateToRegister());
    }
    private UserDAO userDao = new UserDAOManager();

    private void navigateToRegister() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("register.fxml"));
            Parent root = fxmlLoader.load();
            Stage currentStage = (Stage) registerLabel.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Could not navigate to register page: " + e.getMessage());
        }
    }

    @FXML
    private void onKeyPressEvent(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            try {
                handleLogin();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Login failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }
        String rawPassword = passwordField.getText();
        User user = userDao.getUserByUsername(username); // Fetch user from DB
        if (user != null && BCrypt.checkpw(rawPassword, user.getPassword())) {
            FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("/com/rplbo/ukdw/todolistfix/todolist.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            // Here you would typically load the main application view
        } else {
            errorLabel.setText("Invalid username or password");
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