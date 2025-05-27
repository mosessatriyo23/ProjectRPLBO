module com.rplbo.ukdw.todolistfix {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jbcrypt;


    opens com.rplbo.ukdw.todolistfix to javafx.fxml;
    exports com.rplbo.ukdw.todolistfix;
    exports com.rplbo.ukdw.todolistfix.controller;
    opens com.rplbo.ukdw.todolistfix.controller to javafx.fxml;
}