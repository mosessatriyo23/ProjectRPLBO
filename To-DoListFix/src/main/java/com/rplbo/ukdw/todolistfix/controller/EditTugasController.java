package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDAOManager;
import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.model.Task;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class EditTugasController implements Initializable {

    @FXML private TextField judulEditField;
    @FXML private TextArea deskripsiEditArea;
    @FXML private ComboBox<Kategori> kategoriEditComboBox;
    @FXML private CheckBox prioritasEditCheckBox;
    @FXML private DatePicker deadlineDateEditPicker;
    @FXML private Button simpanEditButton;
    @FXML private Button batalEditButton;

    private Task taskToEdit;
    private int currentUserId;
    private TaskDao taskDao;
    private KategoriDao kategoriDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.taskDao = new TaskDAOManager();
        this.kategoriDao = new KategoriDAOManager();
    }

    public void setTaskToEdit(Task task, int userId) {
        System.out.println("[EditTugasController] setTaskToEdit dipanggil.");
        if (task == null) {
        }
        this.taskToEdit = task;
        this.currentUserId = userId;

        if (task != null) {
            judulEditField.setText(task.getJudul());
            deskripsiEditArea.setText(task.getDeskripsi());
            prioritasEditCheckBox.setSelected(task.isPrioritas());

            if (task.getDeadline() != null) {
                deadlineDateEditPicker.setValue(task.getDeadline().toLocalDate());
            } else {
                deadlineDateEditPicker.setValue(null);
            }

            loadKategoriComboBox();
            if (task.getKategoriId() != null && kategoriEditComboBox.getItems() != null) {
                for (Kategori k : kategoriEditComboBox.getItems()) {
                    if (k.getId() == task.getKategoriId()) {
                        kategoriEditComboBox.setValue(k);
                        break;
                    }
                }
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error Data", "Tidak ada data tugas yang valid untuk diedit.");
            closeWindow();
        }
    }

    private void loadKategoriComboBox() {
        if (this.currentUserId == -1) {
            System.err.println("EditTugasController: currentUserId tidak valid saat loadKategoriComboBox.");
            kategoriEditComboBox.setDisable(true);
            kategoriEditComboBox.setPlaceholder(new Label("User tidak valid"));
            return;
        }
        try {
            List<Kategori> kategoriUser = kategoriDao.getKategoriByUserId(this.currentUserId);
            if (kategoriUser != null) {
                kategoriEditComboBox.setItems(FXCollections.observableArrayList(kategoriUser));
            } else {
                kategoriEditComboBox.setItems(FXCollections.emptyObservableList());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar kategori: " + e.getMessage());
        }
    }

    @FXML
    private void handleSimpanEdit(ActionEvent event) {
        if (taskToEdit == null) {
            showAlert(Alert.AlertType.ERROR, "Error Simpan", "Tidak ada tugas yang dipilih untuk disimpan.");
            return;
        }

        String judul = judulEditField.getText().trim();
        String deskripsi = deskripsiEditArea.getText().trim();
        Kategori selectedKategori = kategoriEditComboBox.getValue();
        boolean isPrioritas = prioritasEditCheckBox.isSelected();
        String progressSaatIni = taskToEdit.getProgress();

        if (judul.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Valid", "Judul tugas tidak boleh kosong.");
            return;
        }

        LocalDateTime deadline = null;
        LocalDate selectedDate = deadlineDateEditPicker.getValue();
        if (selectedDate != null) {
            if (taskToEdit.getDeadline() != null && taskToEdit.getDeadline().toLocalDate().equals(selectedDate)) {
                deadline = LocalDateTime.of(selectedDate, taskToEdit.getDeadline().toLocalTime());
            } else {
                deadline = LocalDateTime.of(selectedDate, LocalTime.MIDNIGHT);
            }
        }
        taskToEdit.setDeadline(deadline);

        taskToEdit.setJudul(judul);
        taskToEdit.setDeskripsi(deskripsi);
        taskToEdit.setKategoriId(selectedKategori != null ? selectedKategori.getId() : null);
        taskToEdit.setNamaKategori(selectedKategori != null ? selectedKategori.getNamaKategori() : null); // Update nama kategori juga
        taskToEdit.setPrioritas(isPrioritas);
        taskToEdit.setDeadline(deadline);
        taskToEdit.setProgress(progressSaatIni);

        try {
            boolean success = taskDao.updateTask(taskToEdit);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tugas berhasil diperbarui.");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui tugas di database. Tugas mungkin tidak ditemukan atau tidak ada perubahan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan SQL: " + e.getMessage());
        }
    }

    @FXML
    private void handleBatalEdit(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        if (batalEditButton != null && batalEditButton.getScene() != null) {
            Stage stage = (Stage) batalEditButton.getScene().getWindow();
            stage.close();
        } else if (simpanEditButton != null && simpanEditButton.getScene() != null) {
            Stage stage = (Stage) simpanEditButton.getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
