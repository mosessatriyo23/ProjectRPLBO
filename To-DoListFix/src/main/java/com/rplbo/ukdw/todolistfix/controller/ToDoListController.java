package com.rplbo.ukdw.todolistfix.controller;

import com.rplbo.ukdw.todolistfix.ToDoListApplication;
import com.rplbo.ukdw.todolistfix.dao.KategoriDAOManager;
import com.rplbo.ukdw.todolistfix.dao.KategoriDao;
import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import com.rplbo.ukdw.todolistfix.model.Kategori;
import com.rplbo.ukdw.todolistfix.model.Task;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;

import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ToDoListController implements Initializable {

    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;
    @FXML private Label lblMnthYrCal;
    @FXML private GridPane calenderGrid;
    @FXML private HBox btnHome;
    @FXML private VBox vboxDaftarSemuaTugas;
    @FXML private VBox vboxRingkasanPrioritas;
    @FXML private Label lblSemuaTugas;
    @FXML private Label lblname;
    @FXML private TextField searchbox;
    @FXML private ImageView btnSearch;

    private int currentUserId;
    @FXML private VBox vboxRingkasanKategori;
    private KategoriDao kategoriDao;
    private YearMonth currentYearMonth;
    private final TaskDao taskDao;

    private static final String IMAGE_PATH_PREFIX = "/com/rplbo/ukdw/images/";
    private static final String FXML_PATH_PREFIX = "/com/rplbo/ukdw/todolistfix/";

    public ToDoListController() {
        this.taskDao = new TaskDAOManager();
        this.kategoriDao = new KategoriDAOManager();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startClock();
        this.currentUserId = SessionHelper.getUserId();

        if (this.currentUserId != -1) {
            String name = getUsernameFromDatabase(this.currentUserId);
            if (lblname != null) lblname.setText(name != null ? name : "User");
            refreshAllSummariesAndCalendar();
        } else {
            if (lblname != null) lblname.setText("Guest");
            tampilkanPesanLogin(vboxDaftarSemuaTugas, "Login untuk lihat tugas.");
            if (vboxRingkasanPrioritas != null) tampilkanPesanLogin(vboxRingkasanPrioritas, "Login untuk lihat prioritas.");
            else System.err.println("[ToDoListController] FXML field 'vboxRingkasanPrioritas' adalah null saat initialize (guest).");
            if (vboxRingkasanKategori != null) tampilkanPesanLogin(vboxRingkasanKategori, "Login untuk lihat kategori.");
            if (lblSemuaTugas != null) lblSemuaTugas.setText("Semua tugas (0)");
        }

        currentYearMonth = YearMonth.now();
        if (calenderGrid != null && lblMnthYrCal != null) {
            updateCalendar();
        } else {
            System.err.println("[ToDoListController] Peringatan: Komponen kalender (calenderGrid atau lblMnthYrCal) adalah null saat initialize.");
        }
    }

    public void refreshAllSummariesAndCalendar() {
        if (this.currentUserId != -1) {
            System.out.println("[ToDoListController] Refreshing all summaries and calendar...");
            tampilkanDaftarTugas();
            loadRingkasanPrioritas();
            loadRingkasanKategori();
            if (calenderGrid != null && lblMnthYrCal != null) {
                if(currentYearMonth == null) currentYearMonth = YearMonth.now();
                updateCalendar();
            }
        }
    }

    private void tampilkanDaftarTugas() {
        if (vboxDaftarSemuaTugas == null || lblSemuaTugas == null) return;
        vboxDaftarSemuaTugas.getChildren().clear();

        if (this.currentUserId == -1) {
            lblSemuaTugas.setText("Semua tugas (0)");
            tampilkanPesanLogin(vboxDaftarSemuaTugas, "Login untuk melihat tugas.");
            return;
        }
        try {
            List<Task> semuaTugasAktifPengguna = taskDao.getTaskByUser(this.currentUserId);
            if (semuaTugasAktifPengguna == null) semuaTugasAktifPengguna = Collections.emptyList();

            lblSemuaTugas.setText("Semua tugas (" + semuaTugasAktifPengguna.size() + ")");

            List<Task> tugasUntukDitampilkanDiHome = semuaTugasAktifPengguna.stream()
                    .limit(7)
                    .collect(Collectors.toList());

            if (tugasUntukDitampilkanDiHome.isEmpty()) {
                vboxDaftarSemuaTugas.getChildren().add(new Label("Tidak ada tugas aktif."));
            } else {
                int nomor = 1;
                for (Task tugas : tugasUntukDitampilkanDiHome) {
                    vboxDaftarSemuaTugas.getChildren().add(buatBarisTugas(tugas, nomor++));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar tugas: " + e.getMessage());
            lblSemuaTugas.setText("Semua tugas (Error)");
        }

    }

    private void loadRingkasanPrioritas() {
        if (vboxRingkasanPrioritas == null) {
            System.err.println("[ToDoListController] KRITIS: vboxRingkasanPrioritas adalah NULL. Periksa fx:id di FXML Anda.");
            return;
        }
        vboxRingkasanPrioritas.getChildren().clear();
        System.out.println("[ToDoListController] loadRingkasanPrioritas dipanggil untuk userId: " + this.currentUserId);

        if (this.currentUserId == -1) {
            tampilkanPesanLogin(vboxRingkasanPrioritas, "Login untuk lihat prioritas.");
            return;
        }
        try {
            List<Task> semuaTugasUser = taskDao.getTaskByUser(this.currentUserId); // Hanya tugas aktif
            if (semuaTugasUser == null) {
                System.out.println("[ToDoListController] taskDao.getTaskByUser mengembalikan NULL untuk ringkasan prioritas.");
                semuaTugasUser = Collections.emptyList();
            }
            System.out.println("[ToDoListController] Jumlah semua tugas user (untuk filter prioritas): " + semuaTugasUser.size());
            for(Task t : semuaTugasUser) {
                System.out.println("  - Cek Task: " + t.getJudul() + ", Apakah Prioritas? " + t.isPrioritas());
            }

            List<Task> summaryTasks = semuaTugasUser.stream()
                    .filter(task -> task != null && task.isPrioritas())
                    .limit(3)
                    .collect(Collectors.toList());

            System.out.println("[ToDoListController] Jumlah tugas prioritas ditemukan untuk ringkasan: " + summaryTasks.size());

            if (summaryTasks.isEmpty()) {
                Label placeholder = new Label("Tidak ada tugas prioritas aktif.");
                placeholder.setPadding(new Insets(5)); // Tambahkan padding agar terlihat lebih baik
                vboxRingkasanPrioritas.getChildren().add(placeholder);
            } else {
                int nomor = 1;
                for (Task task : summaryTasks) {
                    System.out.println("[ToDoListController] Menambahkan tugas prioritas ke VBox: " + task.getJudul());
                    HBox barisTugasNode = buatBarisTugas(task, nomor++);
                    if (barisTugasNode != null) {
                        vboxRingkasanPrioritas.getChildren().add(barisTugasNode);
                    } else {
                        System.err.println("[ToDoListController] buatBarisTugas mengembalikan null untuk tugas prioritas: " + task.getJudul());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "DB Error Prioritas", "Gagal memuat tugas prioritas: " + e.getMessage());
            vboxRingkasanPrioritas.getChildren().add(new Label("Error DB memuat prioritas."));
        } catch (Exception e) {
            e.printStackTrace();
            vboxRingkasanPrioritas.getChildren().add(new Label("Error umum memuat prioritas."));
        }
    }

    private void updateCalendar() {
        if (calenderGrid == null || lblMnthYrCal == null || currentYearMonth == null || taskDao == null) {
            System.err.println("[ToDoListController] Komponen kalender atau DAO null di updateCalendar.");
            return;
        }
        calenderGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        lblMnthYrCal.setText(monthName + " " + currentYearMonth.getYear());

        List<Task> allUserTasks;
        if (this.currentUserId != -1) {
            try {
                allUserTasks = taskDao.getTaskByUser(this.currentUserId);
                if (allUserTasks == null) allUserTasks = Collections.emptyList();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat tugas untuk kalender: " + e.getMessage());
                allUserTasks = Collections.emptyList();
            }
        } else {
            allUserTasks = Collections.emptyList();
        }

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int firstDayColumn = (firstDayOfMonth.getDayOfWeek().getValue()) % 7;

        int column = firstDayColumn;
        int row = 1;
        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayCell = new VBox();
            dayCell.setPrefSize(40, 40);
            dayCell.setAlignment(Pos.TOP_CENTER);
            dayCell.setPadding(new Insets(2));

            Label lblTgl = new Label(String.valueOf(day));
            lblTgl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");

            HBox eventBox = new HBox(2);
            eventBox.setAlignment(Pos.CENTER);
            eventBox.setMinHeight(6);

            LocalDate currentDateOfCell = currentYearMonth.atDay(day);
            boolean adaTugasHariIni = false;
            boolean adaTugasPrioritasHariIni = false;

            for (Task task : allUserTasks) {
                if (task.getDeadline() != null && task.getDeadline().toLocalDate().equals(currentDateOfCell)) {
                    adaTugasHariIni = true;
                    if (task.isPrioritas()) {
                        adaTugasPrioritasHariIni = true;
                    }
                }
            }

            if (adaTugasHariIni) {
                Circle circleBiru = new Circle(2, Color.DODGERBLUE);
                eventBox.getChildren().add(circleBiru);
            }
            if (adaTugasPrioritasHariIni) {
                Circle circleOranye = new Circle(2, Color.ORANGE);
                eventBox.getChildren().add(circleOranye);
            }

            dayCell.getChildren().addAll(lblTgl, eventBox);
            calenderGrid.add(dayCell, column, row);
            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }
    }
  
    private HBox buatBarisTugas(Task tugas, int nomor) {
        HBox hboxBaris = new HBox(5);
        hboxBaris.setAlignment(Pos.CENTER_LEFT);
        hboxBaris.setPadding(new Insets(2, 0, 2, 8));

        Label lblNo = new Label(nomor + ".");
        lblNo.setMinWidth(20);

        Node priorityNode;
        if (tugas.isPrioritas()) {
            System.out.println("  [buatBarisTugas] Tugas '" + tugas.getJudul() + "' adalah prioritas. Mencoba load star.png");
            ImageView prioritasIcon = new ImageView();
            Image starImage = loadImage("star.png");
            if (starImage != null && !starImage.isError()) {
                prioritasIcon.setImage(starImage);
                prioritasIcon.setFitWidth(10);
                prioritasIcon.setFitHeight(10);
                priorityNode = prioritasIcon;
                System.out.println("    [buatBarisTugas] Gambar star.png berhasil dimuat.");
            } else {
                System.err.println("    [buatBarisTugas] Gagal memuat star.png atau gambar error. Fallback ke teks.");
                Label starPlaceholder = new Label("★");
                starPlaceholder.setTextFill(Color.ORANGE);
                starPlaceholder.setStyle("-fx-font-size: 10px;");
                priorityNode = starPlaceholder;
            }
        } else {
            Region placeholder = new Region();
            placeholder.setMinWidth(10);
            placeholder.setPrefWidth(10);
            priorityNode = placeholder;
        }

        Label lblJudul = new Label(tugas.getJudul());
        lblJudul.setMaxWidth(150);
        lblJudul.setEllipsisString("...");
        lblJudul.setWrapText(false);

        hboxBaris.getChildren().addAll(lblNo, priorityNode, lblJudul);
        return hboxBaris;
    }

    @FXML public void onSearchBox(ActionEvent actionEvent) { performSearchAndNavigate(); }
    @FXML public void onSearchImg(MouseEvent mouseEvent) { performSearchAndNavigate(); }

    private void performSearchAndNavigate() {
        if (searchbox == null) {
            showAlert(Alert.AlertType.WARNING, "Search Error", "Komponen input pencarian tidak tersedia.");
            return;
        }
        String searchQuery = searchbox.getText().trim();
        try {
            ToDoListApplication.navigateToViewAndPassQuery(
                    "semuatugas.fxml", "Hasil Pencarian Tugas", true, searchQuery,
                    com.rplbo.ukdw.todolistfix.controller.SemuaTugasController.class);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "Gagal membuka halaman hasil pencarian: " + e.getMessage());
        }
    }

    private HBox buatBarisKategori(Kategori kategori, int nomor) {
        HBox hboxBaris = new HBox(5);
        hboxBaris.setAlignment(Pos.CENTER_LEFT);
        Label lblNo = new Label(nomor + ".");
        lblNo.setMinWidth(25);
        HBox.setMargin(lblNo, new Insets(0,0,0,8));
        Label lblNama = new Label(kategori.getNamaKategori());
        HBox.setHgrow(lblNama, Priority.ALWAYS);
        hboxBaris.getChildren().addAll(lblNo, lblNama);
        return hboxBaris;
    }

    private void loadRingkasanKategori() {
        if (vboxRingkasanKategori == null) return;
        vboxRingkasanKategori.getChildren().clear();
        if (this.currentUserId == -1) {
            tampilkanPesanLogin(vboxRingkasanKategori, "Login untuk lihat kategori.");
            return;
        }
        try {
            List<Kategori> daftarKategori = kategoriDao.getKategoriByUserId(this.currentUserId);
            if (daftarKategori != null && !daftarKategori.isEmpty()) {
                int nomor = 1;
                for (Kategori kategori : daftarKategori) {
                    vboxRingkasanKategori.getChildren().add(buatBarisKategori(kategori, nomor++));
                }
            } else {
                vboxRingkasanKategori.getChildren().add(new Label("Tidak ada kategori."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            vboxRingkasanKategori.getChildren().add(new Label("Error memuat kategori."));
        }
    }

    private void tampilkanPesanLogin(VBox container, String message) {
        if (container != null) {
            container.getChildren().clear();
            Label lblLogin = new Label(message);
            lblLogin.setPadding(new Insets(10));
            container.getChildren().add(lblLogin);
        }
    }

    private void startClock() {
        Thread clock = new Thread(() -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
            while (true) {
                if (Thread.currentThread().isInterrupted()) break;
                String time = timeFormat.format(Calendar.getInstance().getTime());
                String tanggal = dateFormat.format(Calendar.getInstance().getTime());
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

    private Image loadImage(String imageName) {
        String fullPath = IMAGE_PATH_PREFIX + imageName;
        try (InputStream is = ToDoListController.class.getResourceAsStream(fullPath)) {
            if (is == null) {
                System.err.println("Resource gambar tidak ditemukan: " + fullPath);
                return null;
            }
            return new Image(is);
        } catch (IOException e) {
            System.err.println("Error memuat gambar " + fullPath + ": " + e.getMessage());
            return null;
        }
    }

    private void loadScene(String fxmlFile) {
        if (btnHome == null || btnHome.getScene() == null) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Navigasi", "Tidak dapat melakukan navigasi.");
            return;
        }
        String fxmlPath = FXML_PATH_PREFIX + fxmlFile;
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) throw new IOException("File FXML tidak ditemukan: " + fxmlPath);
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) btnHome.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Pemuatan", "Gagal memuat tampilan: " + fxmlFile);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getUsernameFromDatabase(int userId) {
        if (userId == -1) return "Guest";
        String username = "User";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT username FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) username = rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Gagal mengambil username dari DB: " + e.getMessage());
        }
        return username;
    }

    @FXML private void handlePrevMnthClick(MouseEvent event) { if(currentYearMonth!=null && calenderGrid != null) currentYearMonth = currentYearMonth.minusMonths(1); updateCalendar(); }
    @FXML private void handleNextMnthClick(MouseEvent event) { if(currentYearMonth!=null && calenderGrid != null) currentYearMonth = currentYearMonth.plusMonths(1); updateCalendar(); }
    @FXML private void handleCalClick(MouseEvent mouseEvent) { if(calenderGrid != null) currentYearMonth = YearMonth.now(); updateCalendar(); }
    @FXML private void handleHomeClick(MouseEvent event) { loadScene("todolist.fxml"); }
    @FXML private void handleExitClick(MouseEvent event) {
        System.out.println("handleExitClick dipanggil!");
        Platform.exit(); }
    @FXML private void handleSemuaTugasClick(MouseEvent event) {
        try {
            ToDoListApplication.navigateToViewAndPassQuery("semuatugas.fxml", "Semua Tugas", true, "", SemuaTugasController.class);
        } catch (IOException e) { e.printStackTrace(); showAlert(Alert.AlertType.ERROR, "Navigasi Error", "Gagal ke Semua Tugas.");}
    }
    @FXML private void handleKategoriClick(MouseEvent event) { loadScene("kategori.fxml"); }
    @FXML private void handlePrioritasClick(MouseEvent event) { loadScene("prioritas.fxml"); }
    @FXML private void handleLogoutClick(MouseEvent event) {
        SessionHelper.clearUserId();
        try {
            ToDoListApplication.setRoot("login", "Login", false);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Gagal kembali ke halaman login.");
        }
    }
    @FXML private void handleTambahTugasClick(MouseEvent event) { loadScene("tambahtugas.fxml"); }
    @FXML private void handleTambahKategoriClick(MouseEvent event) { loadScene("formkategori.fxml"); }
}
