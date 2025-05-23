package com.rplbo.ukdw.todolistfix.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

public class ToDoListController implements Initializable {


    @FXML private Label lblJmMntDtk;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Thread clock = new Thread(() -> {
            while (true) {
                Calendar cal = Calendar.getInstance();
                String time = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
                String tanggal = new SimpleDateFormat("dd MMM yyyy").format(cal.getTime());

                javafx.application.Platform.runLater(() -> {
                    lblJmMntDtk.setText(time);
                    lblTglBlnThn.setText(tanggal);
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        clock.setDaemon(true);
        clock.start();
    }

    @FXML private Label lblTglBlnThn;

    @FXML
    private HBox btnHome;

    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        loadScene("todolistfix/todolist.fxml");
    }

    private void loadScene(String fxmlPath) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("FXML file not found: " + fxmlPath);
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        Stage stage = (Stage) btnHome.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleSemuaTugasClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/semuatugas.fxml");
    }

    @FXML
    private void handleKategoriClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/kategori.fxml");
    }

    @FXML
    private void handlePrioritasClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/prioritas.fxml");
    }

    @FXML
    private void handleLogoutClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/login.fxml");
    }
}