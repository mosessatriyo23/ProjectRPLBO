package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.dao.KategoriDAOManager;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Text;


import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections; // Import untuk Collections.emptyList()
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class KategoriController implements Initializable {

    @FXML private TableView<Kategori> tabell;
    @FXML private TableColumn<Kategori, Void> no;
    @FXML private TableColumn<Kategori, String> namaKategori;
    @FXML private TableColumn<Kategori, String> deskripsi;
    @FXML private TableColumn<Kategori, String> daftarTugasKolom;

    private ObservableList<Kategori> observableDaftarKategori = FXCollections.observableArrayList();
    private KategoriDao kategoriDao;

    @FXML private HBox btnHome;
    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;
    @FXML private Label lblname;

    private int currentUserId;

    private static final String FXML_PATH_PREFIX = "/com/rplbo/ukdw/todolistfix/";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.kategoriDao = new KategoriDAOManager();

        if (tabell == null) {
            System.err.println("FATAL ERROR: TableView 'tabell' tidak ter-inject dari FXML.");
            showAlert(Alert.AlertType.ERROR, "FXML Injection Error", "Komponen tabel utama (tabell) gagal dimuat.");
            return;
        }
        tabell.setItems(observableDaftarKategori);

        this.currentUserId = SessionHelper.getUserId();

        if (lblname != null) {
            if (currentUserId != -1) {
                String name = getUsernameFromDatabase(currentUserId);
                lblname.setText(name != null ? name : "User");
            } else {
                lblname.setText("Guest");
            }
        }

        configureTableColumns();
        loadKategoriData();
        startClock();
    }

    private void configureTableColumns() {
        if (no != null) {
            no.setCellFactory(col -> new TableCell<Kategori, Void>() {
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
        } else {
            System.err.println("Peringatan: TableColumn 'no' tidak ter-inject dari FXML.");
        }

        if (namaKategori != null) {
            namaKategori.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));
        } else {
            System.err.println("Peringatan: TableColumn 'namaKategori' tidak ter-inject dari FXML.");
        }

        if (deskripsi != null) {
            deskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        } else {
            System.err.println("Peringatan: TableColumn 'deskripsi' tidak ter-inject dari FXML.");
        }

        if (daftarTugasKolom != null) {
            daftarTugasKolom.setCellValueFactory(cellData -> {
                Kategori kategori = cellData.getValue();
                if (kategori == null || kategori.getDaftarTugas() == null || kategori.getDaftarTugas().isEmpty()) {
                    return new SimpleStringProperty("-");
                }
                String daftarJudulTugas = kategori.getDaftarTugas().stream()
                        .map(Task::getJudul)
                        .limit(3)
                        .collect(Collectors.joining(", "));
                if (kategori.getDaftarTugas().size() > 3) {
                    daftarJudulTugas += ", ...";
                }
                return new SimpleStringProperty(daftarJudulTugas);
            });

            daftarTugasKolom.setCellFactory(tc -> {
                TableCell<Kategori, String> cell = new TableCell<>();
                Text text = new Text();
                cell.setGraphic(text);
                cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
                text.wrappingWidthProperty().bind(daftarTugasKolom.widthProperty().subtract(10)); // Kurangi sedikit untuk padding
                text.textProperty().bind(cell.itemProperty());
                return cell;
            });
        }
    }

    private void loadKategoriData() {
        observableDaftarKategori.clear();

        if (this.currentUserId == -1) {
            tabell.setPlaceholder(new Label("Silakan login untuk melihat dan mengelola kategori Anda."));
            System.out.println("KategoriController: Pengguna belum login, tidak memuat kategori.");
            return;
        }

        try {
            List<Kategori> kategoriPengguna = kategoriDao.getKategoriByUserId(this.currentUserId);

            if (kategoriPengguna == null) {
                kategoriPengguna = Collections.emptyList();
                System.err.println("KategoriDAO mengembalikan null untuk user ID: " + this.currentUserId);
            }

            observableDaftarKategori.setAll(kategoriPengguna);
            if (tabell != null) {
                tabell.refresh();
                if (kategoriPengguna.isEmpty()) {
                    tabell.setPlaceholder(new Label("Belum ada kategori untuk pengguna ini. Klik 'Tambah Kategori'."));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat Data", "Terjadi kesalahan SQL saat memuat daftar kategori: " + e.getMessage());
            if (tabell != null) {
                tabell.setPlaceholder(new Label("Gagal memuat kategori. Kesalahan database."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Tidak Diketahui", "Terjadi kesalahan tidak diketahui saat memuat kategori: " + e.getMessage());
            if (tabell != null) {
                tabell.setPlaceholder(new Label("Gagal memuat kategori. Kesalahan tidak diketahui."));
            }
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
            e.printStackTrace();
            System.err.println("Gagal mengambil username: " + e.getMessage());
        }
        return "User";
    }

    private void startClock() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            while (true) {
                boolean uiComponentsExist = lblJmMntDtk != null && lblTglBlnThn != null;
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

    private void loadSceneFromEvent(String fxmlFile, MouseEvent event) throws IOException {
        String fxmlPath = FXML_PATH_PREFIX + fxmlFile;
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("FXML file not found: " + fxmlPath);
            showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "File FXML tidak ditemukan: " + fxmlFile);
            return;
        }
        Parent root = FXMLLoader.load(fxmlUrl);
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        if (stage != null) {
            stage.setScene(new Scene(root));
        } else {
            System.err.println("Stage tidak bisa didapatkan dari event source.");
        }
    }

    @FXML private void handleSemuaTugasClick(MouseEvent event) throws IOException { loadSceneFromEvent("semuatugas.fxml", event); }
    @FXML private void handleHomeClick(MouseEvent event) throws IOException { loadSceneFromEvent("todolist.fxml", event); }
    @FXML private void handleKategoriClick(MouseEvent event) throws IOException { loadKategoriData(); }
    @FXML private void handlePrioritasClick(MouseEvent event) throws IOException { loadSceneFromEvent("prioritas.fxml", event); }
    @FXML private void handleLogoutClick(MouseEvent event) {
        SessionHelper.clearUserId();
        try {
            ToDoListApplication.setRoot("login.fxml", "Login", false); // Pastikan path FXML benar
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error","Gagal kembali ke halaman login.");
        }
    }


    @FXML
    private void handleTambahKategori() {
        if (this.currentUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Login Diperlukan", "Anda harus login untuk menambah kategori.");
            return;
        }
        bukaFormKategori(null, "Tambah Kategori Baru");
    }

    @FXML
    private void handleEditKategori() {
        if (this.currentUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Login Diperlukan", "Anda harus login untuk mengedit kategori.");
            return;
        }
        if (tabell == null) return;
        Kategori selectedKategori = tabell.getSelectionModel().getSelectedItem();
        if (selectedKategori != null) {
            if (selectedKategori.getUserId() != this.currentUserId) {
                showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Anda tidak memiliki izin untuk mengedit kategori ini.");
                return;
            }
            bukaFormKategori(selectedKategori, "Edit Kategori");
        } else {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih kategori yang ingin diedit.");
        }
    }

    private void bukaFormKategori(Kategori kategoriToEdit, String windowTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH_PREFIX + "formkategori.fxml"));
            Parent root = loader.load();

            FormKategoriController controller = loader.getController();
            controller.initData(kategoriDao, this, kategoriToEdit);

            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (tabell != null && tabell.getScene() != null && tabell.getScene().getWindow() != null) {
                stage.initOwner(tabell.getScene().getWindow());
            } else if (btnHome != null && btnHome.getScene() != null && btnHome.getScene().getWindow() != null){
                stage.initOwner(btnHome.getScene().getWindow()); // Fallback jika tabel scene null
            }
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Membuka Form", "Tidak dapat membuka form kategori: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Form", "Terjadi kesalahan tidak terduga saat membuka form: " + e.getMessage());
        }
    }

    @FXML
    private void handleHapusKategori() {
        if (this.currentUserId == -1) {
            showAlert(Alert.AlertType.WARNING, "Login Diperlukan", "Anda harus login untuk menghapus kategori.");
            return;
        }
        if (tabell == null) return;
        Kategori selectedKategori = tabell.getSelectionModel().getSelectedItem();
        if (selectedKategori != null) {
            if (selectedKategori.getUserId() != this.currentUserId) {
                showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Anda tidak memiliki izin untuk menghapus kategori ini.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Apakah Anda yakin ingin menghapus kategori '" + selectedKategori.getNamaKategori() + "'?",
                    ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Konfirmasi Hapus");
            confirmAlert.setHeaderText(null);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        boolean berhasilDihapus = kategoriDao.deleteKategori(selectedKategori.getId());
                        if (berhasilDihapus) {
                            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kategori berhasil dihapus.");
                            loadKategoriData();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Gagal", "Kategori tidak ditemukan atau gagal dihapus.");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        if (e.getMessage().toLowerCase().contains("foreign key constraint fail")) {
                            showAlert(Alert.AlertType.ERROR, "Gagal Hapus", "Kategori tidak dapat dihapus karena masih digunakan oleh tugas.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Gagal Hapus", "Terjadi kesalahan SQL: " + e.getMessage());
                        }
                    }
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan pilih kategori yang ingin dihapus.");
        }
    }

    public void refreshTampilanKategori() {
        loadKategoriData();
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