package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.dao.KategoriDAOManager;
import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
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

    @FXML
    private Label lblname;

    @FXML
    private ImageView imguser;

    @FXML
    private TextField txtJudulTugas;

    @FXML
    private TextArea txtDeskripsiTugas;

    @FXML
    private ComboBox<Kategori> kategoriComboBox;

    @FXML
    private CheckBox chkPrioritas;

    @FXML
    private Button btnSmp1;

    @FXML
    private Button btnBatal;

    @FXML
    private Button btnKembali;

    @FXML
    private DatePicker pickDL;

    @FXML
    private HBox btnHome;

    @FXML
    private Label lblJmMntDtk;

    @FXML
    private Label lblTglBlnThn;

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
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Anda harus login untuk menambah tugas.");
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

    private void startClockThread() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
            int loopCount = 0;

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    loopCount++;
                    if (lblJmMntDtk == null || lblTglBlnThn == null) {
                        System.err.println("Clock thread (loop " + loopCount + "): lblJmMntDtk atau lblTglBlnThn adalah null. Menghentikan thread.");
                        break;
                    }
                    Calendar cal = Calendar.getInstance();
                    String time = timeFormat.format(cal.getTime());
                    String tanggal = dateFormat.format(cal.getTime());

                    Platform.runLater(() -> {
                        if (lblJmMntDtk != null) {
                            lblJmMntDtk.setText(time);
                        }
                        if (lblTglBlnThn != null) {
                            lblTglBlnThn.setText(tanggal);
                        }
                    });

                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                System.err.println("Clock thread diinterupsi dan berhenti.");
            } catch (Exception e) {
                System.err.println("Error tak terduga di dalam clock thread: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("Clock thread telah berhenti (setelah loop " + loopCount + " iterasi).");
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

    private void loadKategoriToComboBox() {

        System.out.println("COMBOBOX_DEBUG: Memulai loadKategoriToComboBox(). loggedInUserId: " + this.loggedInUserId);

        if (this.loggedInUserId == -1) {
            observableDaftarKategori.clear();
            kategoriComboBox.setPromptText("Login untuk memilih kategori");
            System.out.println("COMBOBOX_DEBUG: Belum login (loggedInUserId: " + this.loggedInUserId + "), ComboBox dikosongkan.");
            return;
        }

        try {
            System.out.println("COMBOBOX_DEBUG: Mencoba mengambil kategori dari DAO untuk userId: " + this.loggedInUserId);
            List<Kategori> kategoriUser = kategoriDao.getKategoriByUserId(this.loggedInUserId);

            System.out.println("COMBOBOX_DEBUG: DAO mengembalikan " + (kategoriUser == null ? "null" : kategoriUser.size()) + " kategori.");

            if (kategoriUser != null) {
                observableDaftarKategori.setAll(kategoriUser);
                System.out.println("COMBOBOX_DEBUG: ObservableList diisi dengan " + observableDaftarKategori.size() + " kategori.");
            } else {
                observableDaftarKategori.clear();
                System.out.println("COMBOBOX_DEBUG: Daftar kategori dari DAO adalah null, ObservableList dikosongkan.");
            }

            if (observableDaftarKategori.isEmpty()) {
                kategoriComboBox.setPromptText("Belum ada kategori untuk Anda");
                System.out.println("COMBOBOX_DEBUG: Tidak ada kategori di ObservableList, prompt diubah.");
            } else {
                kategoriComboBox.setPromptText("Pilih Kategori");
                System.out.println("COMBOBOX_DEBUG: Kategori dimuat ke ObservableList, prompt default.");
            }

        } catch (SQLException e) {
            System.err.println("COMBOBOX_ERROR: SQLException saat memuat kategori ke ComboBox!");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal memuat daftar kategori ke ComboBox: " + e.getMessage());
            kategoriComboBox.setPromptText("Error memuat kategori");
            observableDaftarKategori.clear();
        } catch (Exception e) {
            System.err.println("COMBOBOX_ERROR: Exception umum saat memuat kategori ke ComboBox!");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Aplikasi", "Terjadi kesalahan: " + e.getMessage());
            kategoriComboBox.setPromptText("Error tak terduga");
            observableDaftarKategori.clear();
        }
    }

    private String getUsernameFromDatabase(int idUser) {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idUser);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database Error saat mengambil username: " + e.getMessage());
        }
        return null;
    }

    @FXML
    private void handleSimpan(ActionEvent event) {
        if (loggedInUserId == -1) {
            showAlert(Alert.AlertType.ERROR, "Gagal Menyimpan", "Tidak ada pengguna yang login atau sesi tidak valid.");
            return;
        }

        String judul = txtJudulTugas.getText().trim();
        String deskripsi = txtDeskripsiTugas.getText().trim();
        Kategori kategoriTerpilih = kategoriComboBox.getSelectionModel().getSelectedItem();
        boolean prioritas = chkPrioritas.isSelected();
        LocalDate deadlineDate = pickDL.getValue();

        if (judul.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Judul tugas harus diisi.");
            txtJudulTugas.requestFocus();
            return;
        }

        if (deadlineDate == null) {
            showAlert(Alert.AlertType.WARNING, "Form Tidak Lengkap", "Deadline harus dipilih.");
            pickDL.requestFocus();
            return;
        }
        LocalDateTime deadline = deadlineDate.atStartOfDay();

        if (kategoriTerpilih == null && observableDaftarKategori != null && !observableDaftarKategori.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Silakan pilih kategori untuk tugas ini.");
            kategoriComboBox.requestFocus();
            return;
        }

        Task newTask = new Task();
        newTask.setJudul(judul);
        newTask.setDeskripsi(deskripsi);
        newTask.setKategoriId(kategoriTerpilih != null ? kategoriTerpilih.getId() : null);
        newTask.setPrioritas(prioritas);
        newTask.setDeadline(deadline);
        newTask.setIdUser(this.loggedInUserId);
        newTask.setProgress("Belum Selesai");

        if (taskDao == null) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Aplikasi", "Task DAO tidak terinisialisasi.");
            return;
        }

        try {
            boolean berhasilDisimpan = taskDao.addTask(newTask);
            if (berhasilDisimpan) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tugas berhasil disimpan!");
                clearForm();
                handleKembali(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal Menyimpan", "Terjadi kesalahan saat menyimpan tugas ke database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Database", "Gagal menyimpan tugas: " + e.getMessage());
        }
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Apakah Anda yakin ingin membatalkan? Semua yang Anda isi akan terhapus.",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Konfirmasi Batal");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                clearForm();
            }
        });
    }

    @FXML
    private void handleKembali(ActionEvent event) {
        try {
            loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/semuatugas.fxml", event);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Kembali", "Tidak dapat memuat halaman Semua Tugas.");
        }
    }

    private void clearForm() {
        txtJudulTugas.clear();
        txtDeskripsiTugas.clear();
        pickDL.setValue(null);
        chkPrioritas.setSelected(false);
        kategoriComboBox.getSelectionModel().clearSelection(); // Juga bersihkan pilihan ComboBox
        kategoriComboBox.setPromptText("Pilih Kategori"); // Kembalikan prompt default
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
            System.err.println("FXML file not found: " + fxmlPath);
            showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "File FXML tidak ditemukan: " + fxmlPath);
            return;
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Stage stage = (Stage) node.getScene().getWindow();
        if (stage != null) {
            stage.setScene(new Scene(root));
        } else {
            System.err.println("Stage tidak ditemukan dari node. Tidak dapat memuat scene.");
        }
    }

    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/todolist.fxml", event);
    }

    @FXML
    private void handleSemuaTugasClick(MouseEvent event) throws IOException {
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/semuatugas.fxml", event);
    }

    @FXML
    private void handleKategoriClick(MouseEvent event) throws IOException {
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/kategori.fxml", event);
    }

    @FXML
    private void handlePrioritasClick(MouseEvent event) throws IOException {
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/prioritas.fxml", event);
    }

    @FXML
    private void handleLogoutClick(MouseEvent event) throws IOException {
        SessionHelper.clearUserId();
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/login.fxml", event);
    }
}