package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public class TambahTugasController {

    @FXML
    private Label lblname;

    @FXML
    private ImageView imguser;

    @FXML
    private TextField txtJudulTugas;

    @FXML
    private TextArea txtDeskripsiTugas;

    @FXML
    private ComboBox<String> cmbKategoriTugas;

    @FXML
    private CheckBox chkPrioritas;

    @FXML
    private Button btnSmp1;

    @FXML
    private Button btnBatal;

    @FXML
    private Button btnKembali;

    @FXML
    private HBox btnHome;



    @FXML
    public void initialize() {
        cmbKategoriTugas.getItems().addAll("Pekerjaan", "Pribadi", "Belajar", "Lainnya");
        cmbKategoriTugas.setValue("Pekerjaan");
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        String judul = txtJudulTugas.getText();
        String deskripsi = txtDeskripsiTugas.getText();
        String kategori = cmbKategoriTugas.getValue();
        boolean prioritas = chkPrioritas.isSelected();

        if (judul.isEmpty() || deskripsi.isEmpty() || kategori == null) {
            showAlert("Form tidak lengkap", "Silakan lengkapi semua kolom sebelum menyimpan.");
            return;
        }

        // Simulasi simpan data
        System.out.println("Tugas Disimpan:");
        System.out.println("Judul: " + judul);
        System.out.println("Deskripsi: " + deskripsi);
        System.out.println("Kategori: " + kategori);
        System.out.println("Prioritas: " + prioritas);

        clearForm();
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi");
        confirmAlert.setHeaderText("Apakah anda yakin untuk membatalkan?");
        confirmAlert.setContentText("Semua yang anda isi akan terhapus");

        ButtonType yesButton = new ButtonType("Ya", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(yesButton, cancelButton);

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                clearForm();
            }
        });
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/rplbo/ukdw/todolistfix/semuatugas.fxml"));   
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Daftar Semua Tugas");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Gagal Kembali", "Tidak dapat memuat halaman semuatugas.fxml");
        }
    }


    private void clearForm() {
        txtJudulTugas.clear();
        txtDeskripsiTugas.clear();
        cmbKategoriTugas.setValue("Pekerjaan");
        chkPrioritas.setSelected(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        loadScene("todolistfix/todolist.fxml");
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
}
