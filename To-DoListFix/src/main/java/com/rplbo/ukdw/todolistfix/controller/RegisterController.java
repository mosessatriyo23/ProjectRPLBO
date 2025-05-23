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
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

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
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {

        loginLabel.setOnMouseClicked(event -> navigateToLogin());
    }
    private UserDAO userDao = new UserDAOManager();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        if (userDao.userExists(username)) {
            errorLabel.setText("Username already exists");
            return;
        }

        String rawPassword = passwordField.getText();
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt()); // Hash the password
        User newUser = new User(username, hashedPassword); // Store the HASHED version
        if (userDao.registerUser(newUser)) {
            errorLabel.setText("Registration successful! Please login.");
        } else {
            errorLabel.setText("Registration failed");
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("login.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
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
