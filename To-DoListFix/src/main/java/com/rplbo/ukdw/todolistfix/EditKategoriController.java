package com.rplbo.ukdw.todolistfix;

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

    private boolean isSaved = false;
    private String originalNama;
    private String originalDeskripsi;

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
        txtNamaKategori.setText(kategori.getNama());
        txtDeskripsi.setText(kategori.getDeskripsi());

        originalNama = kategori.getNama();
        originalDeskripsi = kategori.getDeskripsi();
    }

    @FXML
    private void handleSimpan() {
        String newNama = txtNamaKategori.getText();
        String newDeskripsi = txtDeskripsi.getText();

        boolean changed = !newNama.equals(originalNama) || !newDeskripsi.equals(originalDeskripsi);

        if (changed) {
            kategori.setNama(newNama);
            kategori.setDeskripsi(newDeskripsi);
            isSaved = true;
        } else {
            isSaved = false;
        }

        Stage stage = (Stage) txtNamaKategori.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBatal() {
        isSaved = false;

        Stage stage = (Stage) txtNamaKategori.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return isSaved;
    }
}
