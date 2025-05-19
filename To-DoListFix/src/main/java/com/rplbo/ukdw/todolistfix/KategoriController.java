package com.rplbo.ukdw.todolistfix;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class KategoriController implements Initializable {

    @FXML
    private TableColumn<Kategori, String> deskripsi;

    @FXML
    private TableColumn<Kategori, String> namaKategori;

    @FXML
    private TableColumn<Kategori, Integer> no;

    @FXML
    private TableView<Kategori> tabell;

    @FXML
    private TableColumn<Kategori, String> daftarTugas;

    private ObservableList<Kategori> daftarKategori = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        no.setCellValueFactory(new PropertyValueFactory<>("no"));
        namaKategori.setCellValueFactory(new PropertyValueFactory<>("nama"));
        deskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        daftarTugas.setCellValueFactory(new PropertyValueFactory<>("tugas"));

        // Inisialisasi data dummy
        daftarKategori.addAll(
                new Kategori(1, "Belajar", "Tugas kuliah", "3 tugas"),
                new Kategori(2, "Kerja", "Pekerjaan kantor", "5 tugas")
        );

        tabell.setItems(daftarKategori);

        // Tambahkan event double click untuk edit
        tabell.setRowFactory(tv -> {
            TableRow<Kategori> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Kategori kategori = row.getItem();
                    bukaFormEdit(kategori);
                }
            });
            return row;
        });
    }

    @FXML
    private void handleTambahKategori() {
        bukaFormEdit(null); // null berarti buat baru
    }

    public void tambahKategori(Kategori kategori) {
        daftarKategori.add(kategori);
        tabell.refresh();
    }

    public void updateKategori() {
        tabell.refresh(); // cukup karena objeknya direferensikan langsung
    }

    public int getNextNo() {
        return daftarKategori.size() + 1;
    }

    private void bukaFormEdit(Kategori kategori) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FormKategori.fxml"));
            Parent root = loader.load();

            FormKategoriController controller = loader.getController();
            controller.setParentController(this);
            controller.setKategori(kategori); // bisa null untuk tambah baru

            Stage stage = new Stage();
            stage.setTitle(kategori == null ? "Tambah Kategori" : "Edit Kategori");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHapusKategori() {
        Kategori selected = tabell.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Yakin hapus kategori ini?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    daftarKategori.remove(selected);
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Pilih kategori terlebih dahulu!", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
