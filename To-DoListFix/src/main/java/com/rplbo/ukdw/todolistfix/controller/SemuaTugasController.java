package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil; // Hanya jika getUsernameFromDatabase ada di sini
import com.rplbo.ukdw.todolistfix.util.SessionHelper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality; // Untuk form edit
import javafx.stage.Stage;
// import javafx.event.ActionEvent; // Tidak dipakai jika TextField search tidak punya onAction

import java.io.IOException;
import java.net.URL;
import java.sql.Connection; // Hanya jika getUsernameFromDatabase masih di sini
import java.sql.PreparedStatement; // Hanya jika getUsernameFromDatabase masih di sini
import java.sql.ResultSet; // Hanya jika getUsernameFromDatabase masih di sini
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

public class SemuaTugasController implements Initializable, SearchableController {

    // --- FXML Fields ---
    @FXML private TableView<Task> tabell;
    @FXML private TableColumn<Task, Integer> no;
    @FXML private TableColumn<Task, String> judulTugas;
    @FXML private TableColumn<Task, String> deskripsi;
    @FXML private TableColumn<Task, String> deadline;
    @FXML private TableColumn<Task, String> status;
    @FXML private TableColumn<Task, Void> kolomAksiSelesai;
    @FXML private TableColumn<Task, Void> edit;
    @FXML private TableColumn<Task, Void> hapus;

    @FXML private Button btnTambahTugas;
    @FXML private HBox btnHome;
    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;
    @FXML private Label lblname;
    @FXML private Label lblSumSemuaTugas;
    @FXML private Label lblTugasSelesaiCount;
    @FXML private Label lblPageTitle;
    @FXML private TextField searchFieldSemuaTugas;

    private TaskDao taskDao;
    private int currentidUser = -1;
    private ObservableList<Task> taskList;
    private FilteredList<Task> filteredTasksList;
    private String initialSearchQuery = "";

    @Override
    public void initializeWithSearchQuery(String query) {
        this.initialSearchQuery = (query == null) ? "" : query.trim();
        System.out.println("[SemuaTugasController] initializeWithSearchQuery: Menerima initialSearchQuery: '" + this.initialSearchQuery + "'");

        if (lblPageTitle != null) {
            lblPageTitle.setText(this.initialSearchQuery.isEmpty() ? "Semua Tugas" : "Hasil Pencarian: \"" + this.initialSearchQuery + "\"");
        }
        if (searchFieldSemuaTugas != null) {
            searchFieldSemuaTugas.setText(this.initialSearchQuery);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[SemuaTugasController] initialize CALLED");
        this.taskDao = new TaskDAOManager();
        this.taskList = FXCollections.observableArrayList();
        this.filteredTasksList = new FilteredList<>(this.taskList, task -> true);

        if (tabell != null) {
            tabell.setItems(this.filteredTasksList);
        } else {
            System.err.println("FATAL: TableView 'tabell' adalah null. Periksa fx:id di semuatugas.fxml.");
            showAlert(Alert.AlertType.ERROR, "FXML Error", "Komponen tabel utama (tabell) gagal dimuat.");
            return;
        }

        this.currentidUser = SessionHelper.getUserId();
        if (this.currentidUser != -1) {
            String username = getUsernameFromDatabase(this.currentidUser);
            if (lblname != null) lblname.setText(username != null ? username : "User");
            tabell.setDisable(false);
            loadTugas();
        } else {
            if (lblname != null) lblname.setText("Guest");
            tabell.setDisable(true);
            tabell.setPlaceholder(new Label("Anda harus login untuk melihat tugas."));
            taskList.clear();
            if (lblSumSemuaTugas != null) lblSumSemuaTugas.setText("Tugas Aktif (0)");
            if (lblTugasSelesaiCount != null) lblTugasSelesaiCount.setText("Tugas Selesai: -");
        }

        configureTableColumns();
        setupActionButtons();

        applyFilter(this.initialSearchQuery);

        if (searchFieldSemuaTugas != null) {
            searchFieldSemuaTugas.textProperty().addListener((observable, oldValue, newValue) -> {
                applyFilter(newValue);
            });
        }
        startClockThread();
    }

    private void setupActionButtons() {
        setupEditAndDeleteButtons();
        setupKolomSelesai();
    }

    private void setupKolomSelesai() {
        if (kolomAksiSelesai == null) {
            System.err.println("Peringatan: TableColumn 'kolomAksiSelesai' tidak ter-inject. Pastikan fx:id di FXML sudah benar.");
            return;
        }
        kolomAksiSelesai.setCellFactory(param -> new TableCell<Task, Void>() {
            private final Button btnSelesaikan = new Button("Selesai");
            {
                btnSelesaikan.setOnAction(event -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        Task task = getTableView().getItems().get(getIndex());
                        if (task != null) {
                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                                    "Apakah Anda yakin ingin menandai tugas '" + task.getJudul() + "' sebagai selesai dan menghapusnya?",
                                    ButtonType.YES, ButtonType.NO);
                            confirmAlert.setTitle("Konfirmasi Selesai & Hapus");
                            confirmAlert.setHeaderText(null);
                            confirmAlert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.YES) {
                                    selesaikanDanHapusTugasDariDb(task);
                                }
                            });
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnSelesaikan);
            }
        });
    }

    private void selesaikanDanHapusTugasDariDb(Task task) {
        if (task == null || this.currentidUser == -1) return;
        System.out.println("[SemuaTugasController] Menyelesaikan dan menghapus tugas ID: " + task.getId() + " dari DB.");
        try {
            boolean success = taskDao.deleteTask(task.getId(), this.currentidUser);

            if (success) {
                System.out.println("[SemuaTugasController] Tugas ID: " + task.getId() + " berhasil dihapus dari DB.");
                boolean removed = taskList.remove(task);
                if(removed) {
                    System.out.println("[SemuaTugasController] Tugas ID: " + task.getId() + " dihapus dari taskList UI.");
                } else {
                    System.out.println("[SemuaTugasController] Peringatan: Tugas ID: " + task.getId() + " tidak ditemukan di taskList UI setelah dihapus dari DB. Memuat ulang untuk sinkronisasi...");
                    loadTugas();
                    return;
                }

                if (lblSumSemuaTugas != null) {
                    lblSumSemuaTugas.setText("Tugas Aktif (" + this.filteredTasksList.size() + ")");
                }

                updateTugasSelesaiCountDisplay();

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tugas '" + task.getJudul() + "' telah diselesaikan dan dihapus.");
            } else {
                System.err.println("[SemuaTugasController] Gagal menghapus tugas ID: " + task.getId() + " dari database (DAO mengembalikan false).");
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus tugas dari database. Tugas mungkin tidak ditemukan atau bukan milik Anda.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan SQL saat menghapus tugas: " + e.getMessage());
        }
    }

    private void updateTugasSelesaiCountDisplay() {
        if (lblTugasSelesaiCount == null) return;
        if (this.currentidUser == -1) {
            lblTugasSelesaiCount.setText("Tugas Selesai: -");
            return;
        }
        try {
            int count = taskDao.countTasksByProgress(this.currentidUser, "Selesai");
            lblTugasSelesaiCount.setText("Arsip Selesai: " + count);
            System.out.println("[SemuaTugasController] Jumlah tugas 'Selesai' (di DB): " + count);
        } catch (SQLException e) {
            e.printStackTrace();
            lblTugasSelesaiCount.setText("Tugas Selesai: Error DB");
        }
    }

    private void applyFilter(String query) {
        String lowerCaseFilter = (query == null) ? "" : query.toLowerCase().trim();

        if (this.filteredTasksList == null) {
            System.err.println("[SemuaTugasController] filteredTasksList null di applyFilter.");
            return;
        }

        this.filteredTasksList.setPredicate(task -> {
            if (task == null) return false;

            if (lowerCaseFilter.isEmpty()) {
                return true;
            }

            boolean titleMatch = false;
            if (task.getJudul() != null) {
                titleMatch = task.getJudul().toLowerCase().contains(lowerCaseFilter);
            }

            return titleMatch;
        });

        if (lblSumSemuaTugas != null) {
            lblSumSemuaTugas.setText("Tugas Aktif (" + this.filteredTasksList.size() + ")");
        }
    }

    private void loadTugas() {
        System.out.println("[SemuaTugasController] Memuat tugas untuk userID: " + this.currentidUser);
        if (this.currentidUser == -1) {
            this.taskList.clear();
            if(lblSumSemuaTugas != null) lblSumSemuaTugas.setText("Tugas Aktif (0)");
            if(tabell != null) tabell.setPlaceholder(new Label("Login untuk melihat tugas."));
            updateTugasSelesaiCountDisplay();
            return;
        }
        if(tabell != null) tabell.setPlaceholder(new Label("Memuat tugas..."));

        try {
            List<Task> tugasDariDb = taskDao.getTaskByUser(this.currentidUser);
            this.taskList.setAll(tugasDariDb == null ? FXCollections.emptyObservableList() : tugasDariDb);
            System.out.println("[SemuaTugasController] Selesai memuat tugas. Jumlah (aktif dari DB): " + this.taskList.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat Tugas", "Kesalahan SQL: " + e.getMessage());
            this.taskList.clear();
        }
        updateTugasSelesaiCountDisplay();
    }

    private void configureTableColumns() {
        if (tabell == null) return;
        if (no != null) {
            no.setCellFactory(col -> new TableCell<Task, Integer>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getIndex() < 0) setText(null);
                    else setText(String.valueOf(getTableRow().getIndex() + 1));
                }
            });
        }
        if (judulTugas != null) judulTugas.setCellValueFactory(new PropertyValueFactory<>("judul"));
        if (deskripsi != null) deskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        if (deadline != null) {
            deadline.setCellValueFactory(cellData -> {
                Task task = cellData.getValue();
                if (task == null || task.getDeadline() == null) return new SimpleStringProperty("-");
                return new SimpleStringProperty(task.getDeadline().format(DateTimeFormatter.ofPattern("dd MMM yy HH:mm")));
            });
        }
        if (status != null) {
            status.setCellValueFactory(cellData -> {
                Task task = cellData.getValue();
                if (task == null) return new SimpleStringProperty("");
                return new SimpleStringProperty(task.isPrioritas() ? "★ Prioritas" : "Normal");
            });
        }
    }

    private void setupEditAndDeleteButtons() {
        if (edit != null) {
            edit.setCellFactory(tc -> new TableCell<Task, Void>() {
                private final Button btn = new Button("✎");
                {
                    btn.setOnAction(e -> {
                        if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            Task task = getTableView().getItems().get(getIndex());
                            editTask(task);
                        }
                    });
                    btn.setStyle("-fx-background-color: transparent; -fx-padding: 2 5 2 5; -fx-font-size: 14px;");
                }
                @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : btn); }
            });
        }
        if (hapus != null) {
            hapus.setCellFactory(tc -> new TableCell<Task, Void>() {
                private final Button btn = new Button("🗑");
                {
                    btn.setOnAction(e -> {
                        if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            Task task = getTableView().getItems().get(getIndex());
                            hapusTugasDariDb(task);
                        }
                    });
                    btn.setStyle("-fx-background-color: transparent; -fx-padding: 2 5 2 5; -fx-font-size: 14px; -fx-text-fill: red;");
                }
                @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : btn); }
            });
        }
    }

    private void hapusTugasDariDb(Task task) {
        if (task == null || this.currentidUser == -1) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus tugas '" + task.getJudul() + "' secara permanen?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Hapus Tugas");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean success = taskDao.deleteTask(task.getId(), this.currentidUser);
                    if (success) {
                        taskList.remove(task);
                        if (lblSumSemuaTugas != null) lblSumSemuaTugas.setText("Tugas Aktif (" + this.filteredTasksList.size() + ")");
                        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tugas '" + task.getJudul() + "' berhasil dihapus.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus tugas dari database.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus tugas: " + e.getMessage());
                }
            }
        });
    }

    private void editTask(Task task) {
        if (task == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Tidak ada tugas yang dipilih untuk diedit.");
            System.out.println("[SemuaTugasController] editTask dipanggil dengan task null.");
            return;
        }
        System.out.println("[SemuaTugasController] Mengedit tugas: " + task.getJudul() + " (ID: " + task.getId() + ")");
        try {
            String fxmlPath = "/com/rplbo/ukdw/todolistfix/edittugas.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            EditTugasController editController = loader.getController();
            if (editController != null) {
                System.out.println("[SemuaTugasController] EditTugasController berhasil dimuat. Memanggil setTaskToEdit...");
                editController.setTaskToEdit(task, this.currentidUser);
            } else {
                System.err.println("[SemuaTugasController] FATAL: EditTugasController tidak bisa dimuat dari FXML edittugas.fxml.");
                showAlert(Alert.AlertType.ERROR, "Kesalahan Pemuatan Form", "Gagal memuat komponen form edit.");
                return;
            }

            Stage editStage = new Stage();
            editStage.setTitle("Edit Tugas: " + task.getJudul());
            editStage.setScene(new Scene(root));
            editStage.initModality(Modality.WINDOW_MODAL);
            if (tabell != null && tabell.getScene() != null && tabell.getScene().getWindow() != null) {
                editStage.initOwner(tabell.getScene().getWindow());
            }
            editStage.showAndWait();

            System.out.println("[SemuaTugasController] Window edit ditutup, memuat ulang tugas...");
            loadTugas();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Pemuatan FXML", "Gagal membuka form edit tugas: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Umum", "Terjadi kesalahan tidak terduga saat membuka form edit: " + e.getMessage());
        }
    }

    private String getUsernameFromDatabase(int userId) {
        if (userId == -1) return "Guest";
        String username = "User"; // Default
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[SemuaTugasController] Gagal mengambil username dari DB: " + e.getMessage());
        }
        return username;
    }


    private void loadScene(String fxmlPath) {
        if (btnHome == null || btnHome.getScene() == null) {
            System.err.println("btnHome atau scene-nya null. Tidak bisa navigasi.");
            showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "Tidak dapat melakukan navigasi.");
            return;
        }
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "File tampilan tidak ditemukan: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Pemuatan", "Gagal memuat tampilan: " + fxmlPath);
        }
    }
    private void startClockThread() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
            while (true) {
                Calendar cal = Calendar.getInstance();
                String time = timeFormat.format(cal.getTime());
                String tanggal = dateFormat.format(cal.getTime());
                Platform.runLater(() -> {
                    if(lblJmMntDtk != null) lblJmMntDtk.setText(time);
                    if(lblTglBlnThn != null) lblTglBlnThn.setText(tanggal);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        clock.setDaemon(true);
        clock.start();
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML private void handleSemuaTugasClick(MouseEvent event) {
        try {
            ToDoListApplication.navigateToViewAndPassQuery(
                    "semuatugas.fxml",
                    "Semua Tugas",
                    true,
                    "",
                    SemuaTugasController.class
            );
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal memuat ulang halaman Semua Tugas.");
        }
    }
    @FXML private void handleHomeClick(MouseEvent event) { loadScene("/com/rplbo/ukdw/todolistfix/todolist.fxml"); }
    @FXML private void handleKategoriClick(MouseEvent event) { loadScene("/com/rplbo/ukdw/todolistfix/kategori.fxml"); }
    @FXML private void handlePrioritasClick(MouseEvent event) { loadScene("/com/rplbo/ukdw/todolistfix/prioritas.fxml"); }
    @FXML private void handleLogoutClick(MouseEvent event) {
        SessionHelper.clearUserId();
        try {
            ToDoListApplication.setRoot("login.fxml", "Login", false);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Gagal kembali ke halaman login.");
        }
    }

    public void actionTambahTugas(MouseEvent mouseEvent) {
        System.out.println("[SemuaTugasController] Tombol Tambah Tugas diklik.");
        if (btnTambahTugas == null || btnTambahTugas.getScene() == null) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "Tidak dapat membuka form tambah tugas (referensi tombol null).");
            return;
        }
        try {
            String fxmlPathTambah = "/com/rplbo/ukdw/todolistfix/tambahtugas.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPathTambah));
            Parent root = loader.load();

            Stage currentStage = (Stage) btnTambahTugas.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Tambah Tugas Baru");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Pemuatan FXML", "Gagal memuat halaman tambah tugas: " + e.getMessage());
        }
    }
}