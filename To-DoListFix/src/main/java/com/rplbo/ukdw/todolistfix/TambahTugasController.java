package com.rplbo.ukdw.todolistfix;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

public class TambahTugasController {

    @FXML
    private Label lblname;

    @FXML
    private ImageView imguser;

    @FXML
    private TextField txtJudulTugas;

    @FXML
    private TextField txtDeskripsiTugas;

    @FXML
    private ComboBox<String> cmbKategoriTugas;

    @FXML
    private CheckBox chkPrioritas;

    @FXML
    private Button btnSimpan;

    @FXML
    private Button btnBatal;

    @FXML
    public void initialize() {
        // Inisialisasi kategori jika perlu
        cmbKategoriTugas.getItems().addAll("Pekerjaan", "Pribadi", "Belajar", "Lainnya");
        cmbKategoriTugas.setValue("Pekerjaan");
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        String judul = txtJudulTugas.getText();
        String deskripsi = txtDeskripsiTugas.getText();
        String kategori = cmbKategoriTugas.getValue();
        boolean prioritas = chkPrioritas.isSelected();

        // Validasi input
        if (judul.isEmpty() || deskripsi.isEmpty() || kategori == null) {
            showAlert("Form tidak lengkap", "Silakan lengkapi semua kolom sebelum menyimpan.");
            return;
        }

        // Simpan tugas (logic penyimpanan tergantung implementasi)
        System.out.println("Tugas Disimpan:");
        System.out.println("Judul: " + judul);
        System.out.println("Deskripsi: " + deskripsi);
        System.out.println("Kategori: " + kategori);
        System.out.println("Prioritas: " + prioritas);

        // Reset form atau tutup window, sesuai kebutuhan
        clearForm();
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        clearForm();
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
}
