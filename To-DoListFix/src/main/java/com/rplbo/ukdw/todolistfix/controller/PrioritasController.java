package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Pastikan import ini ada
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell; // Import untuk TableCell
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    @FXML private HBox btnHome;
    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;
    @FXML private Label lblname;
    @FXML private Label lblSumPrioritas;

    @FXML private TableView<Task> tableViewPrioritas;
    @FXML private TableColumn<Task, Void> colNo;
    @FXML private TableColumn<Task, String> colNama;
    @FXML private TableColumn<Task, String> colDeskripsi;
    @FXML private TableColumn<Task, String> colDeadline;
    @FXML private TableColumn<Task, String> colKategori;


    private TaskDao taskDao;
    private int currentUserId = -1;
    private ObservableList<Task> prioritasTaskList;

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
            if (lblSumPrioritas != null) lblSumPrioritas.setText("Prioritas (0)");
        } else {
            String username = getUsernameFromDatabase(currentUserId);
            if (lblname != null) lblname.setText(username != null ? username : "User");
            configureTableColumns();
            loadTugasPrioritas();
        }

        startClockThread();

    }

    private void startClockThread() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.err.println("Clock thread was interrupted and will exit.");
                    break;
                }

                try {
                    if (Platform.isFxApplicationThread() && (lblJmMntDtk == null || lblJmMntDtk.getScene() == null || lblJmMntDtk.getScene().getWindow() == null || !lblJmMntDtk.getScene().getWindow().isShowing())) {

                    }
                } catch (Exception e) {
                    System.err.println("Exception checking UI state in clock thread, exiting: " + e.getMessage());
                    break;
                }


                Calendar cal = Calendar.getInstance();
                String time = timeFormat.format(cal.getTime());
                String tanggal = dateFormat.format(cal.getTime());

                Platform.runLater(() -> {
                    if (lblJmMntDtk != null) lblJmMntDtk.setText(time);
                    if (lblTglBlnThn != null) lblTglBlnThn.setText(tanggal);
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    System.err.println("Clock thread interrupted: " + ex.getMessage());
                    break;
                }
            }
        });
        clock.setDaemon(true);
        clock.start();
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
            // if (fxmlPath.contains("semuatugas")) stage.setTitle("Semua Tugas");
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
        currentUserId = -1;
        loadSceneFromEvent("/com/rplbo/ukdw/todolistfix/login.fxml", event);
    }


    private void configureTableColumns() {
        colNo.setCellFactory(col -> new TableCell<Task, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        colNama.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            return new SimpleStringProperty(task != null && task.getJudul() != null ? task.getJudul() : "");
        });

        colDeskripsi.setCellValueFactory(cellData -> {
            Task task = cellData.getValue();
            return new SimpleStringProperty(task != null && task.getDeskripsi() != null ? task.getDeskripsi() : "");
        });

        if (colDeadline != null) {
            colDeadline.setCellValueFactory(cellData -> {
                Task task = cellData.getValue();
                if (task != null && task.getDeadline() != null) {
                    LocalDateTime deadlineValue = task.getDeadline();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
                    return new SimpleStringProperty(deadlineValue.format(formatter));
                }
                return new SimpleStringProperty("-");
            });
        }

        if (colKategori != null) {
            colKategori.setCellValueFactory(cellData -> {
                Task task = cellData.getValue();
                if (task != null && task.getNamaKategori() != null) {
                    return new SimpleStringProperty(task.getNamaKategori());
                }
                return new SimpleStringProperty(task != null && task.getKategoriId() == null ? "Tanpa Kategori" : "-");
            });
        }
    }

    private String getUsernameFromDatabase(int userId) {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil username dari DB: " + e.getMessage());
        }
        return "User";
    }

    private void loadTugasPrioritas() {
        if (currentUserId == -1) {
            System.out.println("Tidak ada pengguna yang login, tidak memuat tugas prioritas.");
            if (prioritasTaskList != null) prioritasTaskList.clear();
            if (tableViewPrioritas != null) tableViewPrioritas.setPlaceholder(new Label("Login untuk melihat tugas prioritas."));
            if (lblSumPrioritas != null) lblSumPrioritas.setText("Prioritas (0)");
            return;
        }

        if (tableViewPrioritas != null) tableViewPrioritas.setPlaceholder(new Label("Memuat tugas prioritas..."));

        try {
            List<Task> semuaTugasUser = taskDao.getAllTasksByUserId(currentUserId);

            if (semuaTugasUser == null) {
                System.err.println("DAO mengembalikan null untuk daftar tugas userID: " + currentUserId);
                if (prioritasTaskList != null) prioritasTaskList.clear();
                if (tableViewPrioritas != null) tableViewPrioritas.setPlaceholder(new Label("Gagal memuat data (sumber data null)."));
                if (lblSumPrioritas != null) lblSumPrioritas.setText("Prioritas (0)");
                return;
            }

            List<Task> filteredPrioritasTasks = semuaTugasUser.stream()
                    .filter(task -> task != null && task.isPrioritas())
                    .collect(Collectors.toList());

            if (prioritasTaskList != null) {
                prioritasTaskList.setAll(filteredPrioritasTasks);
            } else {
                System.err.println("prioritasTaskList belum diinisialisasi!");
            }

            if (tableViewPrioritas != null && filteredPrioritasTasks.isEmpty()){
                tableViewPrioritas.setPlaceholder(new Label("Tidak ada tugas prioritas."));
            }

            System.out.println("Jumlah tugas prioritas untuk userID " + currentUserId + ": " + filteredPrioritasTasks.size());
            if (lblSumPrioritas != null) {
                lblSumPrioritas.setText("Prioritas (" + filteredPrioritasTasks.size() + ")");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat Tugas Prioritas", "Kesalahan SQL: " + e.getMessage());
            if (prioritasTaskList != null) prioritasTaskList.clear();
            if (tableViewPrioritas != null) tableViewPrioritas.setPlaceholder(new Label("Gagal memuat tugas karena kesalahan database."));
            if (lblSumPrioritas != null) lblSumPrioritas.setText("Prioritas (Error SQL)");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Tidak Diketahui", "Error saat memuat tugas prioritas: " + e.getMessage());
            if (prioritasTaskList != null) prioritasTaskList.clear();
            if (tableViewPrioritas != null) tableViewPrioritas.setPlaceholder(new Label("Gagal memuat tugas karena error tidak diketahui."));
            if (lblSumPrioritas != null) lblSumPrioritas.setText("Prioritas (Error)");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        if (Platform.isFxApplicationThread()) {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(alertType);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }
    }
}