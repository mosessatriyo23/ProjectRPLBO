package com.rplbo.ukdw.todolistfix.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class PrioritasController {

    @FXML
    private Label lblname;

    @FXML
    private TableView<?> tableView;

    @FXML
    private TableColumn<?, ?> colNo;

    @FXML
    private TableColumn<?, ?> colNama;

    @FXML
    private TableColumn<?, ?> colDeskripsi;

}