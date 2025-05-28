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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


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

    @FXML
    private HBox btnHome;

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
        bukaFormEdit(null);
    }

    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/todolist.fxml");
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

    public void tambahKategori(Kategori kategori) {
        daftarKategori.add(kategori);
        tabell.refresh();


        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText("Kategori sudah berhasil ditambahkan!!");
        alert.showAndWait();
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
            controller.setKategori(kategori);

            Stage stage = new Stage();
            stage.setTitle(kategori == null ? "Tambah Kategori" : "Edit Kategori");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditKategori() {
        Kategori selectedKategori = tabell.getSelectionModel().getSelectedItem();

        if (selectedKategori != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("EditKategori.fxml"));
                Parent root = loader.load();

                EditKategoriController controller = loader.getController();
                controller.setKategori(selectedKategori);

                Stage stage = new Stage();
                stage.setTitle("Edit Kategori");
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                if (controller.isSaved()) {
                    tabell.refresh();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("HORE");
                    alert.setHeaderText(null);
                    alert.setContentText("Kategori sudah berhasil di edit!!");
                    alert.showAndWait();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Silakan pilih kategori yang ingin diedit.");
            alert.showAndWait();
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
