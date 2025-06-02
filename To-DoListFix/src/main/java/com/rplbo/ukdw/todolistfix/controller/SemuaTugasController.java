package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

public class SemuaTugasController{

    @FXML
    private TableColumn<?, ?> deskripsi;

    @FXML
    private TableColumn<?, ?> judulTugas;

    @FXML
    private TableColumn<?, Integer> no;

    @FXML
    private TableColumn<?, ?> status;

    @FXML
    private TableView<?> tabell;

    @FXML
    private Button btnTambahTugas;

    @FXML
    private HBox btnHome;

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
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("semuatugas.fxml"));
        Parent root = fxmlLoader.load();
        Stage currentStage = (Stage) btnHome.getScene().getWindow();
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }

    @FXML
    private void handleKategoriClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/kategori.fxml");
    }

    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/todolist.fxml");
    }

    @FXML
    private void handlePrioritasClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/prioritas.fxml");
    }

    @FXML
    private void handleLogoutClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/login.fxml");
    }

    public void handleTambahTugasClick(MouseEvent mouseEvent) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/tambahtugas.fxml");
    }

    public void handleTambahKategoriClick(MouseEvent mouseEvent) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/formkategori.fxml");
    }

    @FXML
    public void actionTambahTugas(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("tambahtugas.fxml"));
        Parent root = fxmlLoader.load();
        Stage currentStage = (Stage) btnTambahTugas.getScene().getWindow();
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }

}
