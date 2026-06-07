package com.rplbo.ukdw.todolistfix.util;

import com.rplbo.ukdw.todolistfix.controller.CustomDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class DialogUtil {

    private static Stage createStage(Parent root) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        scene.getStylesheets().add(
                DialogUtil.class.getResource(
                        "/com/rplbo/ukdw/css/styles.css"
                ).toExternalForm()
        );

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.sizeToScene();
        return stage;
    }

    public static void showSuccess(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DialogUtil.class.getResource(
                            "/com/rplbo/ukdw/todolistfix/customdialog.fxml"));
            Parent root = loader.load();
            CustomDialogController controller = loader.getController();
            controller.setSuccess(title, message);
            createStage(root).showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showError(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DialogUtil.class.getResource(
                            "/com/rplbo/ukdw/todolistfix/customdialog.fxml"));
            Parent root = loader.load();
            CustomDialogController controller = loader.getController();
            controller.setError(title, message);
            createStage(root).showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void showWarning(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DialogUtil.class.getResource(
                            "/com/rplbo/ukdw/todolistfix/customdialog.fxml"));
            Parent root = loader.load();
            CustomDialogController controller = loader.getController();
            controller.setWarning(title, message);
            createStage(root).showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static boolean showConfirm(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DialogUtil.class.getResource(
                            "/com/rplbo/ukdw/todolistfix/customdialog.fxml"));
            Parent root = loader.load();
            CustomDialogController controller = loader.getController();
            controller.setConfirm(title, message);
            Stage stage = createStage(root);
            stage.showAndWait();
            return controller.isConfirmed();
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}