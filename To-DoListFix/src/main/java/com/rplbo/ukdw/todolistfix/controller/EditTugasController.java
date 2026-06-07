package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDAOManager;
import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.util.AiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    // AI fields
    @FXML private Label lblAiResultEdit;
    @FXML private Button btnAiSuggestEdit;

    private com.rplbo.ukdw.todolistfix.model.Task taskToEdit;
    private int currentUserId;
    private TaskDao taskDao;
    private KategoriDao kategoriDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.taskDao = new TaskDAOManager();
        this.kategoriDao = new KategoriDAOManager();
    }

    public void setTaskToEdit(com.rplbo.ukdw.todolistfix.model.Task task, int userId) {
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
            showAlert(Alert.AlertType.ERROR, "Error Data",
                    "Tidak ada data tugas yang valid untuk diedit.");
            closeWindow();
        }
    }

    // ===== AI FEATURE =====

    @FXML
    private void handleAiSuggestEdit(ActionEvent event) {
        String judul = judulEditField.getText().trim();
        String deskripsi = deskripsiEditArea.getText().trim();

        if (judul.isEmpty()) {
            if (lblAiResultEdit != null) {
                lblAiResultEdit.setText("⚠️ Isi judul tugas terlebih dahulu.");
            }
            return;
        }

        if (lblAiResultEdit != null) {
            lblAiResultEdit.setText("⏳ AI sedang menganalisis perubahan...");
        }

        if (btnAiSuggestEdit != null) {
            btnAiSuggestEdit.setDisable(true);
        }

        String deadline = deadlineDateEditPicker.getValue() != null
                ? deadlineDateEditPicker.getValue().toString()
                : "";

        Task<String> aiTask = new Task<>() {
            @Override
            protected String call() {
                return AiService.suggestTask(judul, deskripsi);
            }
        };

        aiTask.setOnSucceeded(e -> {
            if (lblAiResultEdit != null) {
                lblAiResultEdit.setText(aiTask.getValue());
            }

            if (btnAiSuggestEdit != null) {
                btnAiSuggestEdit.setDisable(false);
            }
        });

        aiTask.setOnFailed(e -> {
            if (lblAiResultEdit != null) {
                lblAiResultEdit.setText("⚠️ Gagal mendapatkan saran AI.");
            }

            if (btnAiSuggestEdit != null) {
                btnAiSuggestEdit.setDisable(false);
            }
        });

        Thread thread = new Thread(aiTask);
        thread.setDaemon(true);
        thread.start();
    }

    // ===== EXISTING METHODS =====

    private void loadKategoriComboBox() {
        if (this.currentUserId == -1) {
            kategoriEditComboBox.setDisable(true);
            return;
        }

        try {
            List<Kategori> kategoriUser =
                    kategoriDao.getKategoriByUserId(this.currentUserId);

            if (kategoriUser != null) {
                kategoriEditComboBox.setItems(
                        FXCollections.observableArrayList(kategoriUser)
                );
            } else {
                kategoriEditComboBox.setItems(
                        FXCollections.emptyObservableList()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    "Gagal memuat daftar kategori: " + e.getMessage());
        }
    }

    @FXML
    private void handleSimpanEdit(ActionEvent event) {
        if (taskToEdit == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Error Simpan",
                    "Tidak ada tugas yang dipilih.");
            return;
        }

        String judul = judulEditField.getText().trim();
        String deskripsi = deskripsiEditArea.getText().trim();
        Kategori selectedKategori = kategoriEditComboBox.getValue();
        boolean isPrioritas = prioritasEditCheckBox.isSelected();
        String progressSaatIni = taskToEdit.getProgress();

        if (judul.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Input Tidak Valid",
                    "Judul tugas tidak boleh kosong.");
            return;
        }

        LocalDateTime deadline = null;
        LocalDate selectedDate = deadlineDateEditPicker.getValue();

        if (selectedDate != null) {
            if (taskToEdit.getDeadline() != null
                    && taskToEdit.getDeadline().toLocalDate().equals(selectedDate)) {

                deadline = LocalDateTime.of(
                        selectedDate,
                        taskToEdit.getDeadline().toLocalTime()
                );

            } else {
                deadline = LocalDateTime.of(
                        selectedDate,
                        LocalTime.MIDNIGHT
                );
            }
        }

        taskToEdit.setJudul(judul);
        taskToEdit.setDeskripsi(deskripsi);
        taskToEdit.setKategoriId(
                selectedKategori != null ? selectedKategori.getId() : null
        );
        taskToEdit.setNamaKategori(
                selectedKategori != null
                        ? selectedKategori.getNamaKategori()
                        : null
        );
        taskToEdit.setPrioritas(isPrioritas);
        taskToEdit.setDeadline(deadline);
        taskToEdit.setProgress(progressSaatIni);

        try {
            boolean success = taskDao.updateTask(taskToEdit);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Sukses",
                        "Tugas berhasil diperbarui.");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR,
                        "Gagal",
                        "Gagal memperbarui tugas di database.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    "Terjadi kesalahan SQL: " + e.getMessage());
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

    private void showAlert(Alert.AlertType alertType,
                           String title,
                           String content) {

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}