package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.util.AiService;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class FormKategoriController {

    @FXML private TextField txtNamaKategori;
    @FXML private TextArea txtDeskripsi;
    @FXML private Button btnSimpan;

    // AI fields
    @FXML private Label lblAiKategori;
    @FXML private Button btnAiKategori;

    private KategoriController kategoriControllerParent;
    private KategoriDao kategoriDao;
    private Kategori kategoriEdit;
    private boolean isEditMode = false;
    private int currentUserId;

    public void initData(KategoriDao dao, KategoriController parent, Kategori kategoriToEdit) {
        this.kategoriDao = dao;
        this.kategoriControllerParent = parent;
        this.kategoriEdit = kategoriToEdit;
        this.currentUserId = SessionHelper.getUserId();

        if (this.currentUserId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Sesi pengguna tidak ditemukan. Silakan login kembali.");
            closeForm();
            return;
        }

        if (kategoriToEdit != null) {
            this.isEditMode = true;
            txtNamaKategori.setText(kategoriToEdit.getNamaKategori());
            if (txtDeskripsi != null && kategoriToEdit.getDeskripsi() != null) {
                txtDeskripsi.setText(kategoriToEdit.getDeskripsi());
            }
            if (btnSimpan != null) btnSimpan.setText("Update");
        } else {
            this.isEditMode = false;
            txtNamaKategori.clear();
            if (txtDeskripsi != null) txtDeskripsi.clear();
            if (btnSimpan != null) btnSimpan.setText("Simpan");
        }
    }

    // ===== AI FEATURE =====

    @FXML
    private void handleAiKategoriSuggest(ActionEvent event) {
        String nama = txtNamaKategori.getText().trim();
        if (nama.isEmpty()) {
            if (lblAiKategori != null) lblAiKategori.setText("⚠️ Isi nama kategori terlebih dahulu.");
            return;
        }
        if (lblAiKategori != null) lblAiKategori.setText("⏳ AI sedang membuat saran...");
        if (btnAiKategori != null) btnAiKategori.setDisable(true);

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() {
                return AiService.suggestKategoriDescription(nama);
            }
        };
        aiTask.setOnSucceeded(e -> {
            String suggestion = aiTask.getValue();
            if (lblAiKategori != null) lblAiKategori.setText(suggestion);
            // Auto-fill description field
            if (txtDeskripsi != null && txtDeskripsi.getText().trim().isEmpty()) {
                txtDeskripsi.setText(suggestion);
            }
            if (btnAiKategori != null) btnAiKategori.setDisable(false);
        });
        aiTask.setOnFailed(e -> {
            if (lblAiKategori != null) lblAiKategori.setText("⚠️ Gagal mendapatkan saran AI.");
            if (btnAiKategori != null) btnAiKategori.setDisable(false);
        });
        Thread thread = new Thread(aiTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        closeForm();
    }

    // ===== EXISTING METHODS =====

    @FXML
    public void simpanData(ActionEvent event) {
        String namaKategori = txtNamaKategori.getText().trim();
        String deskripsi = (txtDeskripsi != null) ? txtDeskripsi.getText().trim() : "";

        if (namaKategori.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Nama Kategori tidak boleh kosong.");
            txtNamaKategori.requestFocus();
            return;
        }
        if (this.currentUserId == -1) {
            showAlert(Alert.AlertType.ERROR, "Error Sesi", "Sesi pengguna tidak valid.");
            return;
        }

        try {
            boolean sukses;
            String pesanAksi;
            Kategori kategoriProses;

            if (isEditMode) {
                if (kategoriEdit == null || kategoriEdit.getId() == 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Data kategori untuk diedit tidak valid.");
                    return;
                }
                kategoriProses = new Kategori(kategoriEdit.getId(), namaKategori, deskripsi, kategoriEdit.getUserId());
                sukses = kategoriDao.updateKategori(kategoriProses);
                pesanAksi = "diperbarui";
            } else {
                kategoriProses = new Kategori(namaKategori, deskripsi, this.currentUserId);
                sukses = kategoriDao.addKategori(kategoriProses);
                pesanAksi = "ditambahkan";
            }

            if (sukses) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil " + pesanAksi + ".");
                if (kategoriControllerParent != null) kategoriControllerParent.refreshTampilanKategori();
                closeForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Operasi kategori gagal.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan SQL: " + e.getMessage());
        }
    }

    private void closeForm() {
        Node sourceNode = btnSimpan != null ? btnSimpan : txtNamaKategori;
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
