package com.rplbo.ukdw.todolistfix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;


import java.net.URL;
import java.util.ResourceBundle;

public class KategoriController{

    @FXML
    private TableColumn<?, ?> deskripsi;

    @FXML
    private TableColumn<?, ?> namaKategori;

    @FXML
    private TableColumn<?, ?> no;

    @FXML
    private TableView<?> tabell;

    @FXML
    private TableColumn<?, ?> daftarTugas;

}
