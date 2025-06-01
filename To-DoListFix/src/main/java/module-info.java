module com.rplbo.ukdw.todolistfix {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jbcrypt;
    requires java.prefs;


    opens com.rplbo.ukdw.todolistfix to javafx.fxml;
    opens com.rplbo.ukdw.todolistfix.model to javafx.base;
    exports com.rplbo.ukdw.todolistfix;
    exports com.rplbo.ukdw.todolistfix.controller;
    opens com.rplbo.ukdw.todolistfix.controller to javafx.fxml;
}