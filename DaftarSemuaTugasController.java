package com.rplbo.ukdw.todolistprojek;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class DaftarSemuaTugasController implements Initializable {

    @FXML
    private TableColumn<User, String> deskripsi;

    @FXML
    private TableColumn<User, String> judulTugas;

    @FXML
    private TableColumn<User, Integer> no;

    @FXML
    private TableColumn<User, String> status;

    @FXML
    private TableView<User> tabell;


    ObservableList<User> data = FXCollections.observableArrayList(
            new User(1, "Kerjain project", "Tugas RPLBO", "Selesai"),
            new User(2, "Bikin poster", "Poster USDA BEM", "Selesai"),
            new User(3, "Kerjain quiznya coy", "Quiz Pemrograman Web", "Belum Selesai"),
            new User(4, "Ada post test yang harus diselesaikan", "Post Test Pemrograman Web", "Belum Selesai"),
            new User(5, "Kerjain UG PrakRPL", "UnGuided Praktikum RPLBO", "Belum Selesai")

    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deskripsi.setCellValueFactory(new PropertyValueFactory<User, String>("deskripsi"));
        judulTugas.setCellValueFactory(new PropertyValueFactory<User, String>("judulTugas"));
        no.setCellValueFactory(new PropertyValueFactory<User, Integer>("no"));
        status.setCellValueFactory(new PropertyValueFactory<User, String>("status"));

        tabell.setItems(data);

    }
}
