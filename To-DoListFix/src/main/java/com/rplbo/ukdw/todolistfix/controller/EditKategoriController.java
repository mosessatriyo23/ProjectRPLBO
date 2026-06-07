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
import com.rplbo.ukdw.todolistfix.util.DialogUtil;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class EditKategoriController {

    @FXML
    private TextField txtIdKategori;
    @FXML
    private TextField txtNamaKategori;
    @FXML
    private TextArea txtDeskripsi;
    @FXML
    private Button btnSimpan;

    // AI fields
    @FXML
    private Label lblAiEditKategori;
    @FXML
    private Button btnAiEditKategori;

    private KategoriController kategoriControllerParent;
    private KategoriDao kategoriDao;
    private Kategori kategoriToEdit;
    private String originalNamaKategori;
    private String originalDeskripsi;
    private boolean isSaved = false;
    private int currentUserId;

    public void initData(KategoriDao dao, KategoriController parent, Kategori kategori) {
        this.kategoriDao = dao;
        this.kategoriControllerParent = parent;
        this.kategoriToEdit = kategori;
        this.currentUserId = SessionHelper.getUserId();

        if (this.kategoriToEdit != null) {
            if (txtIdKategori != null) {
                txtIdKategori.setText(String.valueOf(this.kategoriToEdit.getId()));
                txtIdKategori.setDisable(true);
            }
            txtNamaKategori.setText(this.kategoriToEdit.getNamaKategori());
            originalNamaKategori = this.kategoriToEdit.getNamaKategori();

            if (txtDeskripsi != null) {
                String deskripsiAsli = this.kategoriToEdit.getDeskripsi();
                txtDeskripsi.setText(deskripsiAsli != null ? deskripsiAsli : "");
                originalDeskripsi = deskripsiAsli != null ? deskripsiAsli : "";
            }
            if (btnSimpan != null) btnSimpan.setText("Simpan");
        } else {
            DialogUtil.showError(
                    "Error Inisialisasi",
                    "Data kategori untuk diedit tidak ditemukan."
            );
            closeForm();
        }
    }

    // ===== AI FEATURE =====

    @FXML
    private void handleAiEditKategori(ActionEvent event) {
        String nama = txtNamaKategori.getText().trim();
        String deskripsiSaatIni = txtDeskripsi != null ? txtDeskripsi.getText().trim() : "";

        if (nama.isEmpty()) {
            if (lblAiEditKategori != null) lblAiEditKategori.setText("⚠️ Isi nama kategori terlebih dahulu.");
            return;
        }
        if (lblAiEditKategori != null) lblAiEditKategori.setText("⏳ AI sedang memperbaiki deskripsi...");
        if (btnAiEditKategori != null) btnAiEditKategori.setDisable(true);

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() {
                return AiService.improveKategoriDescription(nama, deskripsiSaatIni);
            }
        };
        aiTask.setOnSucceeded(e -> {
            String improved = aiTask.getValue();
            if (lblAiEditKategori != null) lblAiEditKategori.setText(improved);
            if (txtDeskripsi != null) txtDeskripsi.setText(improved);
            if (btnAiEditKategori != null) btnAiEditKategori.setDisable(false);
        });
        aiTask.setOnFailed(e -> {
            if (lblAiEditKategori != null) lblAiEditKategori.setText("⚠️ Gagal mendapatkan saran AI.");
            if (btnAiEditKategori != null) btnAiEditKategori.setDisable(false);
        });
        Thread thread = new Thread(aiTask);
        thread.setDaemon(true);
        thread.start();
    }

    // ===== EXISTING METHODS =====

    @FXML
    private void handleSimpan(ActionEvent event) {
        DialogUtil.showSuccess(
                "TEST",
                "INI CUSTOM DIALOG"
        );
        if (kategoriToEdit == null) {
            DialogUtil.showError(
                    "Error",
                    "Data kategori tidak valid."
            );
            return;
        }
        if (this.currentUserId == -1 || kategoriToEdit.getUserId() != this.currentUserId) {
            DialogUtil.showError(
                    "Akses Ditolak",
                    "Anda tidak memiliki izin untuk mengedit kategori ini."
            );
            return;
        }

        String newNama = txtNamaKategori.getText().trim();
        String newDeskripsi = txtDeskripsi != null ? txtDeskripsi.getText().trim() : "";

        if (newNama.isEmpty()) {
            DialogUtil.showWarning(
                    "Input Tidak Valid",
                    "Nama Kategori tidak boleh kosong."
            );
            txtNamaKategori.requestFocus();
            return;
        }

        boolean namaChanged = !newNama.equals(originalNamaKategori);
        boolean deskripsiChanged = !newDeskripsi.equals(originalDeskripsi);
        if (!namaChanged && !deskripsiChanged) {
            DialogUtil.showWarning(
                    "Info",
                    "Tidak ada perubahan data."
            );
            return;
        }

        Kategori updated = new Kategori(kategoriToEdit.getId(), newNama, newDeskripsi, kategoriToEdit.getUserId());
        try {
            boolean sukses = kategoriDao.updateKategori(updated);
            if (sukses) {
                isSaved = true;
                DialogUtil.showSuccess(
                        "Sukses",
                        "Kategori berhasil diperbarui."
                );
                if (kategoriControllerParent != null) kategoriControllerParent.refreshTampilanKategori();
                closeForm();
            } else {
                DialogUtil.showError(
                        "Gagal",
                        "Gagal memperbarui kategori."
                );
            }
        } catch (SQLException e) {
            isSaved = false;
            e.printStackTrace();
            DialogUtil.showError(
                    "Database Error",
                    "Terjadi kesalahan SQL: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        closeForm();
    }

    private void closeForm() {
        Node sourceNode = btnSimpan != null ? btnSimpan : txtNamaKategori;
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    public boolean isSaved() {
        return isSaved;
    }
}
