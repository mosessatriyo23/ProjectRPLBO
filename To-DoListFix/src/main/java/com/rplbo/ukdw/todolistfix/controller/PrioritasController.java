package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.AiService;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PrioritasController implements Initializable {

    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;
    @FXML private Label lblname;
    @FXML private Label lblSumPrioritas;

    @FXML private TableView<Task> tableViewPrioritas;
    @FXML private TableColumn<Task, Void>   colNo;
    @FXML private TableColumn<Task, String> colNama;
    @FXML private TableColumn<Task, String> colDeskripsi;
    @FXML private TableColumn<Task, String> colDeadline;
    @FXML private TableColumn<Task, String> colKategori;

    // AI fields
    @FXML private Label  lblAiPrioritasSummary;
    @FXML private Button btnAiAnalyze;

    private TaskDao taskDao;
    private int currentUserId = -1;
    private ObservableList<Task> prioritasTaskList;

    // ===== INIT =====

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.taskDao = new TaskDAOManager();
        this.prioritasTaskList = FXCollections.observableArrayList();
        tableViewPrioritas.setItems(prioritasTaskList);
        this.currentUserId = SessionHelper.getUserId();

        if (currentUserId == -1) {
            if (lblname != null) lblname.setText("Guest");
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Anda harus login untuk melihat tugas prioritas.");
            tableViewPrioritas.setPlaceholder(new Label("Silakan login untuk melihat tugas prioritas."));
            if (lblSumPrioritas != null) lblSumPrioritas.setText("Semua Prioritas");
        } else {
            String username = getUsernameFromDatabase(currentUserId);
            if (lblname != null) lblname.setText(username != null ? username : "User");
            configureTableColumns();
            loadTugasPrioritas();
        }
        startClockThread();
    }

    // ===== AI FEATURE =====

    @FXML
    private void handleAiAnalyzePrioritas(ActionEvent event) {
        if (prioritasTaskList == null || prioritasTaskList.isEmpty()) {
            if (lblAiPrioritasSummary != null)
                lblAiPrioritasSummary.setText("⚠️ Belum ada tugas prioritas untuk dianalisis.");
            return;
        }

        if (lblAiPrioritasSummary != null) lblAiPrioritasSummary.setText("⏳ AI sedang menganalisis tugas prioritasmu...");
        if (btnAiAnalyze != null) btnAiAnalyze.setDisable(true);

        // Build a compact task summary string
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(prioritasTaskList.size(), 8); i++) {
            Task t = prioritasTaskList.get(i);
            sb.append((i + 1)).append(". ").append(t.getJudul());
            if (t.getDeadline() != null) {
                sb.append(" (deadline: ")
                  .append(t.getDeadline().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                  .append(")");
            }
            if (i < prioritasTaskList.size() - 1) sb.append("; ");
        }
        if (prioritasTaskList.size() > 8) sb.append(" ...dan ").append(prioritasTaskList.size() - 8).append(" lainnya.");

        String tasksSummary = sb.toString();

        javafx.concurrent.Task<String> aiTask = new javafx.concurrent.Task<>() {
            @Override
            protected String call() {
                return AiService.analyzePriorityTasks(tasksSummary);
            }
        };

        aiTask.setOnSucceeded(e -> {
            if (lblAiPrioritasSummary != null) lblAiPrioritasSummary.setText(aiTask.getValue());
            if (btnAiAnalyze != null) btnAiAnalyze.setDisable(false);
        });

        aiTask.setOnFailed(e -> {
            if (lblAiPrioritasSummary != null) lblAiPrioritasSummary.setText("⚠️ Gagal mendapatkan analisis AI. Cek koneksi internet.");
            if (btnAiAnalyze != null) btnAiAnalyze.setDisable(false);
        });

        Thread thread = new Thread(aiTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML private void handleExitClick(MouseEvent event) {
        System.out.println("handleExitClick dipanggil!");
        Platform.exit();
    }

    @FXML private void handleLogoutClick(MouseEvent event) {
        SessionHelper.clearUserId();
        try {
            ToDoListApplication.setRoot("login", "Login", false);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Gagal kembali ke halaman login.");
        }
    }

    // ===== TABLE SETUP =====

    private void configureTableColumns() {
        colNo.setCellFactory(col -> new TableCell<Task, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()
                        ? null : String.valueOf(getIndex() + 1));
            }
        });

        colNama.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue() != null && cd.getValue().getJudul() != null
                        ? cd.getValue().getJudul() : ""));

        colDeskripsi.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue() != null && cd.getValue().getDeskripsi() != null
                        ? cd.getValue().getDeskripsi() : ""));

        if (colDeadline != null) {
            colDeadline.setCellValueFactory(cd -> {
                Task t = cd.getValue();
                if (t != null && t.getDeadline() != null)
                    return new SimpleStringProperty(t.getDeadline().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
                return new SimpleStringProperty("-");
            });
        }

        if (colKategori != null) {
            colKategori.setCellValueFactory(cd -> {
                Task t = cd.getValue();
                if (t != null && t.getNamaKategori() != null)
                    return new SimpleStringProperty(t.getNamaKategori());
                return new SimpleStringProperty(t != null && t.getKategoriId() == null ? "Tanpa Kategori" : "-");
            });
        }
    }

    // ===== DATA =====

    private void loadTugasPrioritas() {
        if (currentUserId == -1) {
            prioritasTaskList.clear();
            tableViewPrioritas.setPlaceholder(new Label("Login untuk melihat tugas prioritas."));
            if (lblSumPrioritas != null) lblSumPrioritas.setText("Semua Prioritas (0)");
            return;
        }

        tableViewPrioritas.setPlaceholder(new Label("Memuat tugas prioritas..."));

        try {
            List<Task> all = taskDao.getAllTasksByUserId(currentUserId);
            if (all == null) {
                prioritasTaskList.clear();
                tableViewPrioritas.setPlaceholder(new Label("Gagal memuat data."));
                return;
            }

            List<Task> filtered = all.stream()
                    .filter(t -> t != null && t.isPrioritas())
                    .collect(Collectors.toList());

            prioritasTaskList.setAll(filtered);

            if (filtered.isEmpty())
                tableViewPrioritas.setPlaceholder(new Label("Belum ada tugas prioritas."));

            if (lblSumPrioritas != null)
                lblSumPrioritas.setText("Semua Prioritas (" + filtered.size() + ")");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Database", "Kesalahan SQL: " + e.getMessage());
            prioritasTaskList.clear();
        }
    }

    // ===== NAVIGATION =====

    @FXML private void handleHomeClick(MouseEvent event)       throws IOException { loadScene("/com/rplbo/ukdw/todolistfix/todolist.fxml",    event); }
    @FXML private void handleSemuaTugasClick(MouseEvent event) throws IOException { loadScene("/com/rplbo/ukdw/todolistfix/semuatugas.fxml",  event); }
    @FXML private void handleKategoriClick(MouseEvent event)   throws IOException { loadScene("/com/rplbo/ukdw/todolistfix/kategori.fxml",    event); }
    @FXML private void handlePrioritasClick(MouseEvent event)  throws IOException { loadScene("/com/rplbo/ukdw/todolistfix/prioritas.fxml",   event); }


    private void loadScene(String fxmlPath, MouseEvent event) throws IOException {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) { showAlert(Alert.AlertType.ERROR, "Error", "FXML tidak ditemukan: " + fxmlPath); return; }
        Parent root = FXMLLoader.load(url);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // ===== UTIL =====

    private void startClockThread() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yy");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Calendar cal = Calendar.getInstance();
                    String time = tf.format(cal.getTime());
                    String date = df.format(cal.getTime());
                    Platform.runLater(() -> {
                        if (lblJmMntDtk != null) lblJmMntDtk.setText(time);
                        if (lblTglBlnThn != null) lblTglBlnThn.setText(date);
                    });
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        });
        clock.setDaemon(true);
        clock.start();
    }

    private String getUsernameFromDatabase(int userId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("username");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "User";
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        if (Platform.isFxApplicationThread()) {
            new Alert(type, msg) {{ setTitle(title); setHeaderText(null); }}.showAndWait();
        } else {
            Platform.runLater(() -> new Alert(type, msg) {{ setTitle(title); setHeaderText(null); }}.showAndWait());
        }
    }
}
