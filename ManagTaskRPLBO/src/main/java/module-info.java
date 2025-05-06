module com.rplbo.ukdw.managtaskrplbo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.rplbo.ukdw.managtaskrplbo to javafx.fxml;
    exports com.rplbo.ukdw.managtaskrplbo;
}