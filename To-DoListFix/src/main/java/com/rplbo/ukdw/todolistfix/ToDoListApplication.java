package com.rplbo.ukdw.todolistfix;

import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ToDoListApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseUtil.initializeDatabase();
        primaryStage = stage;
        primaryStage.setTitle("Title");
        primaryStage.setScene(new Scene(loadFXML("todolist")));
        primaryStage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        String path = "/com/rplbo/ukdw/todolistfix/" + fxml + ".fxml";
        URL fxmlLocation = ToDoListApplication.class.getResource(path);
        if (fxmlLocation == null) {
            throw new IllegalStateException("FXML file not found at path: " + path);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        return fxmlLoader.load();
    }

    public static void setRoot(String fxml, String title, boolean isResizeable) throws IOException {
        primaryStage.getScene().setRoot(loadFXML(fxml));
        primaryStage.sizeToScene();
        primaryStage.setResizable(isResizeable);
        if (title != null) {
            primaryStage.setTitle(title);
        }
        primaryStage.show();
    }

    public static void openViewWithModal(String fxml, boolean isResizeable) throws IOException {
        Stage stage = new Stage();
        stage.setScene(new Scene(loadFXML(fxml)));
        stage.sizeToScene();
        stage.setResizable(isResizeable);
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}