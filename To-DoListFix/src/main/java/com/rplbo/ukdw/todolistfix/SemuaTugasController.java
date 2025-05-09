package com.rplbo.ukdw.todolistfix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

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

}
