package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.util.SessionHelper; // Import SessionHelper
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class FormKategoriController {

    @FXML
    private TextField txtNamaKategori;

    @FXML
    private TextField txtDeskripsi;

    @FXML
    private Button btnSimpan;

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

        if (kategoriToEdit != null) { // Mode Edit
            this.isEditMode = true;
            txtNamaKategori.setText(kategoriToEdit.getNamaKategori());
            if (txtDeskripsi != null && kategoriToEdit.getDeskripsi() != null) {
                txtDeskripsi.setText(kategoriToEdit.getDeskripsi());
            } else if (txtDeskripsi != null) {
                txtDeskripsi.clear();
            }
            if(btnSimpan != null) btnSimpan.setText("Update");
        } else {
            this.isEditMode = false;
            txtNamaKategori.clear();
            if(txtDeskripsi != null) txtDeskripsi.clear();
            if(btnSimpan != null) btnSimpan.setText("Simpan");
        }
    }

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
            showAlert(Alert.AlertType.ERROR, "Error Sesi", "Tidak dapat menyimpan kategori, sesi pengguna tidak valid.");
            return;
        }

        Kategori kategoriProses;

        try {
            boolean sukses;
            String pesanAksi;

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
                if (sukses && kategoriProses.getId() != 0) {
                    System.out.println("Kategori baru '" + kategoriProses.getNamaKategori() + "' ditambahkan dengan ID: " + kategoriProses.getId() + " untuk User ID: " + kategoriProses.getUserId());
                }
            }

            if (sukses) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil " + pesanAksi + ".");
                if (kategoriControllerParent != null) {
                    kategoriControllerParent.refreshTampilanKategori();
                }
                closeForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Operasi kategori gagal. Cek kembali data Anda.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            String detailError = e.getMessage().toLowerCase();
            if (detailError.contains("unique constraint") || detailError.contains("duplicate entry") || detailError.contains("primary key") || detailError.contains("nama_kategori")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan: Nama Kategori '" + namaKategori + "' mungkin sudah ada untuk pengguna ini.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan SQL: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Tidak Diketahui", "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleBatalForm(ActionEvent event) {
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
        } else {
            System.err.println("FormKategoriController: Tidak bisa menutup form, scene tidak ditemukan.");
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