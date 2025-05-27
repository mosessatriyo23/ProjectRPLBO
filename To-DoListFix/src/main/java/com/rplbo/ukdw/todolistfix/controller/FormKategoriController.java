package com.rplbo.ukdw.todolistfix.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormKategoriController {
    @FXML
    private TextField txtNama;

    @FXML
    private TextField txtDeskripsi;

    @FXML
    private TextField txtTugas;

    private KategoriController parentController;
    private com.rplbo.ukdw.todolistfix.Kategori kategoriEdit;

    public void setParentController(KategoriController controller) {
        this.parentController = controller;
    }

    public void setKategori(com.rplbo.ukdw.todolistfix.Kategori kategori) {
        if (kategori != null) {
            kategoriEdit = kategori;
            txtNama.setText(kategori.getNama());
            txtDeskripsi.setText(kategori.getDeskripsi());
            txtTugas.setText(kategori.getTugas());
        }
    }

    @FXML
    public void simpanData() {
        String nama = txtNama.getText();
        String deskripsi = txtDeskripsi.getText();
        String tugas = txtTugas.getText();

        if (kategoriEdit != null) {
            kategoriEdit.setNama(nama);
            kategoriEdit.setDeskripsi(deskripsi);
            kategoriEdit.setTugas(tugas);

        } else {
            int no = parentController.getNextNo();
            com.rplbo.ukdw.todolistfix.Kategori kategoriBaru = new com.rplbo.ukdw.todolistfix.Kategori(no, nama, deskripsi, tugas);
            parentController.tambahKategori(kategoriBaru);
        }

        Stage stage = (Stage) txtNama.getScene().getWindow();
        stage.close();
    }
}
