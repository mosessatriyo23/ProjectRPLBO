package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDAOManager;
import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.util.AiService;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import com.rplbo.ukdw.todolistfix.util.DialogUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Node;


import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

public class TambahTugasController {

    @FXML private Label lblname;
    @FXML private ImageView imguser;
    @FXML private TextField txtJudulTugas;
    @FXML private TextArea txtDeskripsiTugas;
    @FXML private ComboBox<Kategori> kategoriComboBox;
    @FXML private CheckBox chkPrioritas;
    @FXML private Button btnSmp1;
    @FXML private Button btnBtl1;
    @FXML private Button btnKembali;
    @FXML private DatePicker pickDL;
    @FXML private HBox btnHome;
    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;

    // AI fields
    @FXML private Label lblAiResult;
    @FXML private Label lblAiPriority;
    @FXML private Button btnAiSuggest;

    private String aiPrioritySuggestion = "";
    private TaskDao taskDao;
    private int loggedInUserId = -1;
    private KategoriDao kategoriDao;
    private ObservableList<Kategori> observableDaftarKategori;

    @FXML
    public void initialize() {
        this.taskDao = new TaskDAOManager();
        this.loggedInUserId = SessionHelper.getUserId();

        if (this.loggedInUserId == -1) {
            lblname.setText("Guest");
            DialogUtil.showError(
                    "Gagal Menyimpan",
                    "Tidak ada pengguna yang login."
            );
            btnSmp1.setDisable(true);
        } else {
            String username = getUsernameFromDatabase(this.loggedInUserId);
            lblname.setText(username != null ? username : "User");
            btnSmp1.setDisable(false);
        }

        startClockThread();
        kategoriDao = new KategoriDAOManager();
        observableDaftarKategori = FXCollections.observableArrayList();
        kategoriComboBox.setItems(observableDaftarKategori);
        configureKategoriComboBox();
        loadKategoriToComboBox();
    }

    // ===== AI FEATURE =====

    @FXML
    private void handleAiSuggest(ActionEvent event) {
        String judul = txtJudulTugas.getText().trim();
        String deskripsi = txtDeskripsiTugas.getText().trim();

        if (judul.isEmpty()) {
            if (lblAiResult != null) lblAiResult.setText("⚠️ Isi judul tugas terlebih dahulu agar AI bisa memberi saran.");
            return;
        }

        if (lblAiResult != null) lblAiResult.setText("⏳ AI sedang menganalisis...");
        if (btnAiSuggest != null) btnAiSuggest.setDisable(true);

        String deadline = pickDL.getValue() != null ? pickDL.getValue().toString() : "";

        javafx.concurrent.Task<String[]> aiTask = new javafx.concurrent.Task<>() {
            @Override
            protected String[] call() {
                String suggestion = AiService.suggestTask(judul, deskripsi);
                String priority = AiService.suggestPriority(judul, deskripsi, deadline);

                return new String[]{
                        suggestion,
                        priority
                };
            }
        };

        aiTask.setOnSucceeded(e -> {
            String[] results = aiTask.getValue();
            if (lblAiResult != null) lblAiResult.setText(results[0]);
            if (lblAiPriority != null) lblAiPriority.setText(results[1]);
            aiPrioritySuggestion = results[1].toLowerCase();
            if (btnAiSuggest != null) btnAiSuggest.setDisable(false);
        });

        aiTask.setOnFailed(e -> {
            if (lblAiResult != null) lblAiResult.setText("⚠️ Gagal mendapatkan saran AI. Cek koneksi internet.");
            if (btnAiSuggest != null) btnAiSuggest.setDisable(false);
        });

        Thread thread = new Thread(aiTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleAiApplyPriority(ActionEvent event) {
        if (aiPrioritySuggestion.contains("prioritas") && !aiPrioritySuggestion.contains("tidak")) {
            chkPrioritas.setSelected(true);
        } else {
            chkPrioritas.setSelected(false);
        }
    }

    // ===== EXISTING METHODS =====

    private void startClockThread() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (lblJmMntDtk == null || lblTglBlnThn == null) break;
                    Calendar cal = Calendar.getInstance();
                    String time = timeFormat.format(cal.getTime());
                    String tanggal = dateFormat.format(cal.getTime());
                    Platform.runLater(() -> {
                        if (lblJmMntDtk != null) lblJmMntDtk.setText(time);
                        if (lblTglBlnThn != null) lblTglBlnThn.setText(tanggal);
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        clock.setDaemon(true);
        clock.start();
    }

    private void configureKategoriComboBox() {
        kategoriComboBox.setCellFactory(listView -> new ListCell<Kategori>() {
            @Override
            protected void updateItem(Kategori item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaKategori());
            }
        });
        kategoriComboBox.setButtonCell(new ListCell<Kategori>() {
            @Override
            protected void updateItem(Kategori item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaKategori());
            }
        });
    }

    @FXML
    private void handleSimpan(ActionEvent event) {

        if (loggedInUserId == -1) {
            DialogUtil.showError(
                    "Gagal Menyimpan",
                    "Tidak ada pengguna yang login."
            );
            return;
        }

        String judul = txtJudulTugas.getText().trim();
        String deskripsi = txtDeskripsiTugas.getText().trim();

        Kategori kategoriTerpilih =
                kategoriComboBox.getSelectionModel().getSelectedItem();

        boolean prioritas =
                chkPrioritas.isSelected();

        LocalDate deadlineDate =
                pickDL.getValue();

        if (judul.isEmpty()) {
            DialogUtil.showWarning(
                    "Form Tidak Lengkap",
                    "Judul tugas harus diisi."
            );
            txtJudulTugas.requestFocus();
            return;
        }

        if (deadlineDate == null) {
            DialogUtil.showWarning(
                    "Form Tidak Lengkap",
                    "Deadline harus dipilih."
            );
            return;
        }

        if (kategoriTerpilih == null
                && !observableDaftarKategori.isEmpty()) {

            DialogUtil.showWarning(
                    "Input Tidak Lengkap",
                    "Silakan pilih kategori."
            );

            kategoriComboBox.requestFocus();
            return;
        }

        LocalDateTime deadline =
                deadlineDate.atStartOfDay();

        Task newTask = new Task();

        newTask.setJudul(judul);
        newTask.setDeskripsi(deskripsi);
        newTask.setKategoriId(
                kategoriTerpilih != null
                        ? kategoriTerpilih.getId()
                        : null
        );
        newTask.setPrioritas(prioritas);
        newTask.setDeadline(deadline);
        newTask.setIdUser(loggedInUserId);
        newTask.setProgress("Belum Selesai");

        try {

            boolean berhasil =
                    taskDao.addTask(newTask);

            if (berhasil) {

                DialogUtil.showSuccess(
                        "Berhasil",
                        "Tugas berhasil disimpan!"
                );

                clearForm();
                handleKembali(event);

            } else {

                DialogUtil.showError(
                        "Gagal",
                        "Terjadi kesalahan saat menyimpan tugas."
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

            DialogUtil.showError(
                    "Database Error",
                    e.getMessage()
            );
        }
    }

    private void loadKategoriToComboBox() {
        if (this.loggedInUserId == -1) {
            observableDaftarKategori.clear();
            kategoriComboBox.setPromptText("Login untuk memilih kategori");
            return;
        }
        try {
            List<Kategori> kategoriUser = kategoriDao.getKategoriByUserId(this.loggedInUserId);
            if (kategoriUser != null) {
                observableDaftarKategori.setAll(kategoriUser);
            } else {
                observableDaftarKategori.clear();
            }
            if (observableDaftarKategori.isEmpty()) {
                kategoriComboBox.setPromptText("Belum ada kategori untuk Anda");
            } else {
                kategoriComboBox.setPromptText("Pilih Kategori");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            DialogUtil.showError(
                    "Error Database",
                    "Gagal memuat daftar kategori: " + e.getMessage()
            );
        }
    }

    private String getUsernameFromDatabase(int idUser) {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUser);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Apakah Anda yakin ingin membatalkan?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Konfirmasi Batal");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) clearForm();
        });
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        try {
            loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/semuatugas.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
            DialogUtil.showError(
                    "Gagal Kembali",
                    "Tidak dapat memuat halaman Semua Tugas."
            );
        }
    }

    private void clearForm() {
        txtJudulTugas.clear();
        txtDeskripsiTugas.clear();
        pickDL.setValue(null);
        chkPrioritas.setSelected(false);
        kategoriComboBox.getSelectionModel().clearSelection();
        kategoriComboBox.setPromptText("Pilih Kategori");
        if (lblAiResult != null) lblAiResult.setText("Klik tombol 'Analisis & Beri Saran' untuk mendapatkan rekomendasi AI.");
        if (lblAiPriority != null) lblAiPriority.setText("AI akan menyarankan apakah tugas ini perlu ditandai prioritas.");
        aiPrioritySuggestion = "";
    }

    private void loadSceneFromEvent(String fxmlPath, ActionEvent event) throws IOException {
        loadSceneFromNode(fxmlPath, (Node) event.getSource());
    }
    private void loadSceneFromEvent(String fxmlPath, MouseEvent event) throws IOException {
        loadSceneFromNode(fxmlPath, (Node) event.getSource());
    }

    private void loadSceneFromNode(String fxmlPath, Node node) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            DialogUtil.showError(
                    "Kesalahan Navigasi",
                    "File FXML tidak ditemukan: " + fxmlPath
            );
            return;
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Stage stage = (Stage) node.getScene().getWindow();
        if (stage != null) stage.setScene(new Scene(root));
    }

    @FXML private void handleHomeClick(MouseEvent event) throws IOException { loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/todolist.fxml", event); }
    @FXML private void handleSemuaTugasClick(MouseEvent event) throws IOException { loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/semuatugas.fxml", event); }
    @FXML private void handleKategoriClick(MouseEvent event) throws IOException { loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/kategori.fxml", event); }
    @FXML private void handlePrioritasClick(MouseEvent event) throws IOException { loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/prioritas.fxml", event); }
    @FXML private void handleLogoutClick(MouseEvent event) throws IOException {
        SessionHelper.clearUserId();
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/login.fxml", event);
    }
}
