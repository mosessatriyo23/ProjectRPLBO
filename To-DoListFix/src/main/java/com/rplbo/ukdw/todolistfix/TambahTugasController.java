package com.rplbo.ukdw.todolistfix;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

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
    private ComboBox<String> comboKategori;

    @FXML
    private Button btnSimpan;

    @FXML
    private Button btnBatal;

    @FXML
    private void initialize() {}
}