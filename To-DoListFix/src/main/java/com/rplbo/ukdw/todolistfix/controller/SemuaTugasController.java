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
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

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
    public void actionTambahTugas(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("tambahtugas.fxml"));
        Parent root = fxmlLoader.load();
        Stage currentStage = (Stage) btnTambahTugas.getScene().getWindow();
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }

}
