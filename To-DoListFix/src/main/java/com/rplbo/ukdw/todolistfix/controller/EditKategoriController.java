package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;


import java.sql.SQLException;

public class EditKategoriController {

    @FXML
    private TextField txtIdKategori;

    @FXML
    private TextField txtNamaKategori;

    @FXML
    private TextField txtDeskripsi;

    @FXML
    private Button btnSimpan;

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
            if (btnSimpan != null) btnSimpan.setText("Update");

        } else {
            showAlert(Alert.AlertType.ERROR, "Error Inisialisasi", "Data kategori untuk diedit tidak ditemukan.");
            closeForm();
        }
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        if (kategoriToEdit == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Data kategori untuk diedit tidak valid.");
            return;
        }

        if (this.currentUserId == -1 || kategoriToEdit.getUserId() != this.currentUserId) {
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Anda tidak memiliki izin untuk mengedit kategori ini.");
            return;
        }

        String newNamaKategori = txtNamaKategori.getText().trim();
        String newDeskripsi = "";
        if (txtDeskripsi != null) {
            newDeskripsi = txtDeskripsi.getText().trim();
        }

        if (newNamaKategori.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Nama Kategori tidak boleh kosong.");
            txtNamaKategori.requestFocus();
            return;
        }

        boolean namaChanged = !newNamaKategori.equals(originalNamaKategori);
        boolean deskripsiChanged = !newDeskripsi.equals(originalDeskripsi);

        if (!namaChanged && !deskripsiChanged) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "Tidak ada perubahan data.");
            return;
        }

        Kategori updatedKategori = new Kategori(
                kategoriToEdit.getId(),
                newNamaKategori,
                newDeskripsi,
                kategoriToEdit.getUserId() // Pertahankan userId pemilik asli
        );

        try {
            boolean sukses = kategoriDao.updateKategori(updatedKategori);
            if (sukses) {
                isSaved = true;
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil diperbarui.");
                if (kategoriControllerParent != null) {
                    kategoriControllerParent.refreshTampilanKategori();
                }
                closeForm();
            } else {
                isSaved = false;
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui kategori. Kategori mungkin tidak ditemukan atau tidak ada perubahan.");
            }
        } catch (SQLException e) {
            isSaved = false;
            e.printStackTrace();
            String detailError = e.getMessage().toLowerCase();
            if (detailError.contains("unique constraint") || detailError.contains("duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui: Nama Kategori '" + newNamaKategori + "' mungkin sudah ada untuk pengguna ini.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan SQL: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) { // Jika ada tombol batal
        closeForm();
    }

    private void closeForm() {
        Node sourceNode = btnSimpan;
        if (sourceNode == null && txtNamaKategori != null) {
            sourceNode = txtNamaKategori;
        }
        if (sourceNode != null && sourceNode.getScene() != null) {
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    public boolean isSaved(y) {
        return isSaved;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
