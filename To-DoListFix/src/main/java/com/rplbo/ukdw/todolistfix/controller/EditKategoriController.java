package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.Kategori;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditKategoriController {
    @FXML
    private TextField txtNamaKategori;

    @FXML
    private TextArea txtDeskripsi;

    private Kategori kategori;

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
        txtNamaKategori.setText(kategori.getNama());
        txtDeskripsi.setText(kategori.getDeskripsi());
    }

    @FXML
    private void handleSimpan() {
        kategori.setNama(txtNamaKategori.getText());
        kategori.setDeskripsi(txtDeskripsi.getText());
        Stage stage = (Stage) txtNamaKategori.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) txtNamaKategori.getScene().getWindow();
        stage.close();
    }
}
