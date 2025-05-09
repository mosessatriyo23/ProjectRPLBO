package com.rplbo.ukdw.todolistprojek;

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

public class KategoriController implements Initializable {

    @FXML
    private TableColumn<UserKategori, String> deskripsi;

    @FXML
    private TableColumn<UserKategori, String> namaKategori;

    @FXML
    private TableColumn<UserKategori, Integer> no;

    @FXML
    private TableView<UserKategori> tabell;

    @FXML
    private TableColumn<UserKategori, String> daftarTugas;


    ObservableList<UserKategori> data = FXCollections.observableArrayList(
            new UserKategori("Ini khusus buat perkuliahan aja brok", "Kuliah", 1, "Tugas RPLBO, Quiz Pemrograman Web, " +
                    "Post Test Pemrograman Web, UnGuided Praktikum RPLBO"),
            new UserKategori("Ini khusus buat ngantoran aja brok", "Kantor", 2, "Poster USDA BEM")

    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deskripsi.setCellValueFactory(new PropertyValueFactory<UserKategori, String>("deskripsi"));
        namaKategori.setCellValueFactory(new PropertyValueFactory<UserKategori, String>("namaKategori"));
        no.setCellValueFactory(new PropertyValueFactory<UserKategori, Integer>("no"));
        daftarTugas.setCellValueFactory(new PropertyValueFactory<UserKategori, String>("daftarTugas"));

        tabell.setItems(data);

        tabell.setItems(data);
        autosizeColumns(tabell);
    }

    public static void autosizeColumns(TableView<?> table) {
        table.getColumns().forEach(column -> {
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();

            for (int i = 0; i < table.getItems().size(); i++) {
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double width = t.getLayoutBounds().getWidth();
                    if (width > max) {
                        max = width;
                    }
                }
            }

            column.setPrefWidth(max + 20); // + padding

        });


    }



}
