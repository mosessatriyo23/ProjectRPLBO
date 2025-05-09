module com.rplbo.ukdw.todolistfix {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.rplbo.ukdw.todolistfix to javafx.fxml;
    exports com.rplbo.ukdw.todolistfix;
}