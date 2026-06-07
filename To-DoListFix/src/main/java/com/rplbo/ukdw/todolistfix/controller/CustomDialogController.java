package com.rplbo.ukdw.todolistfix.controller;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CustomDialogController {

    @FXML private VBox dialogCard;

    @FXML private Label lblIcon;
    @FXML private Label lblTitle;
    @FXML private Label lblMessage;

    @FXML private Button btnOk;
    @FXML private Button btnCancel;

    private boolean confirmed = false;

    @FXML
    public void initialize() {

        ScaleTransition scale =
                new ScaleTransition(
                        Duration.millis(220),
                        dialogCard
                );

        scale.setFromX(0.85);
        scale.setFromY(0.85);
        scale.setToX(1);
        scale.setToY(1);

        scale.play();
    }

    public void setSuccess(
            String title,
            String message
    ){
        lblIcon.setText("✓");
        lblIcon.getStyleClass().add("dialog-success");

        lblTitle.setText(title);
        lblMessage.setText(message);
    }

    public void setWarning(
            String title,
            String message
    ){
        lblIcon.setText("⚠");
        lblIcon.getStyleClass().add("dialog-warning");

        lblTitle.setText(title);
        lblMessage.setText(message);
    }

    public void setError(
            String title,
            String message
    ){
        lblIcon.setText("✕");
        lblIcon.getStyleClass().add("dialog-error");

        lblTitle.setText(title);
        lblMessage.setText(message);
    }

    public void setConfirm(
            String title,
            String message
    ){
        lblIcon.setText("?");
        lblIcon.getStyleClass().add("dialog-confirm");

        lblTitle.setText(title);
        lblMessage.setText(message);

        btnCancel.setVisible(true);
        btnCancel.setManaged(true);

        btnOk.setText("Ya");
    }

    @FXML
    private void handleOk(){
        confirmed = true;
        close();
    }

    @FXML
    private void handleCancel(){
        confirmed = false;
        close();
    }

    private void close(){
        ((Stage)btnOk.getScene()
                .getWindow())
                .close();
    }

    public boolean isConfirmed(){
        return confirmed;
    }
}