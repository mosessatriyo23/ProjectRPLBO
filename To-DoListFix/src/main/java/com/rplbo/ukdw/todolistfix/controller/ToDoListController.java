package com.rplbo.ukdw.todolistfix;

import com.rplbo.ukdw.todolistfix.dao.TaskDAOManager;
import com.rplbo.ukdw.todolistfix.dao.TaskDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.ResourceBundle;

public class ToDoListController implements Initializable {

    @FXML private Label lblJmMntDtk;
    @FXML private Label lblTglBlnThn;
    @FXML private Label lblMnthYrCal;
    @FXML private GridPane calenderGrid;
    @FXML private HBox btnHome;

    private YearMonth currentYearMonth;
    private TaskDao taskDao = new TaskDAOManager();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set waktu dan tanggal real-time
        Thread clock = new Thread(() -> {
            while (true) {
                Calendar cal = Calendar.getInstance();
                String time = new SimpleDateFormat("HH:mm:ss").format(cal.getTime());
                String tanggal = new SimpleDateFormat("dd MMM yyyy").format(cal.getTime());

                Platform.runLater(() -> {
                    lblJmMntDtk.setText(time);
                    lblTglBlnThn.setText(tanggal);
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        clock.setDaemon(true);
        clock.start();

        currentYearMonth = YearMonth.now();
        updateCalendar();
    }

    private void updateCalendar() {
        calenderGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        lblMnthYrCal.setText(
                currentYearMonth.getMonth().name().substring(0, 1).toUpperCase() +
                        currentYearMonth.getMonth().name().substring(1).toLowerCase() +
                        " " + currentYearMonth.getYear()
        );

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        DayOfWeek startDay = firstDayOfMonth.getDayOfWeek();

        int firstDayColumn = (startDay.getValue()) % 7; // convert Monday=1 -> Sunday=0

        int column = firstDayColumn;
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayCell = new VBox();
            dayCell.setPrefSize(40, 40); // Sesuaikan ukuran
            dayCell.setStyle("-fx-alignment: top-center; -fx-padding: 2;");

            Label lblTgl = new Label(String.valueOf(day));
            lblTgl.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");

            HBox eventBox = new HBox();
            eventBox.setSpacing(3);
            eventBox.setStyle("-fx-alignment: center;");

            boolean adaTugas = false;
            boolean adaPrioritas = false;

            Circle event = new Circle(2, javafx.scene.paint.Color.DODGERBLUE);
            Circle prio = new Circle(2, javafx.scene.paint.Color.ORANGE);

            eventBox.getChildren().addAll(event, prio);

            dayCell.getChildren().addAll(lblTgl, eventBox);

            calenderGrid.add(dayCell, column, row);

            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }
    }

    @FXML
    private void handlePrevMnthClick(MouseEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMnthClick(MouseEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleCalClick(MouseEvent mouseEvent) {
        currentYearMonth = YearMonth.now();
        updateCalendar();
    }

    private void loadScene(String fxmlPath) throws IOException {
        URL fxmlUrl = getClass().getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("FXML file not found: " + fxmlPath);
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        Stage stage = (Stage) btnHome.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    private void handleHomeClick(MouseEvent event) throws IOException {
        loadScene("todolistfix/todolist.fxml");
    }

    @FXML
    private void handleSemuaTugasClick(MouseEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("semuatugas.fxml"));
        Parent root = fxmlLoader.load();
        Stage currentStage = (Stage) btnHome.getScene().getWindow();
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }

    @FXML
    private void handleKategoriClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/kategori.fxml");
    }

    @FXML
    private void handlePrioritasClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/prioritas.fxml");
    }

    @FXML
    private void handleLogoutClick(MouseEvent event) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/login.fxml");
    }

    public void handleTambahTugasClick(MouseEvent mouseEvent) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/tambahtugas.fxml");
    }

    public void handleTambahKategoriClick(MouseEvent mouseEvent) throws IOException {
        loadScene("/com/rplbo/ukdw/todolistfix/formkategori.fxml");
    }
}