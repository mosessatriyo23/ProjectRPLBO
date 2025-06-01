package com.rplbo.ukdw.todolistfix;

import com.rplbo.ukdw.todolistfix.controller.SearchableController;
import com.rplbo.ukdw.todolistfix.util.DatabaseUtil;
import com.rplbo.ukdw.todolistfix.util.SessionHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ToDoListApplication extends Application {
    private static Stage primaryStage;
    private static double xOffset = 0;
    private static double yOffset = 0;
    public static final String FXML_PATH_PREFIX = "/com/rplbo/ukdw/todolistfix/";

    @Override
    public void start(Stage stage) throws IOException {
        Platform.setImplicitExit(false);
        stage.initStyle(StageStyle.UNDECORATED);

        try (InputStream iconStream = ToDoListApplication.class.getResourceAsStream("/icons/app_logo.png")) {
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                if (appIcon.isError()) {
                    System.err.println("Error ikon utama: " + (appIcon.getException() != null ? appIcon.getException().getMessage() : "Unknown"));
                } else {
                    stage.getIcons().add(appIcon);
                }
            } else {
                System.err.println("Resource ikon utama tidak ditemukan: /icons/app_logo.png");
            }
        } catch (Exception e) {
            System.err.println("Exception ikon utama: " + e.getMessage());
        }

        primaryStage = stage;
        DatabaseUtil.initializeDatabase();
        int savedUserId = SessionHelper.getUserId();
        Parent root;
        String initialFxmlBaseName;

        if (savedUserId != -1) {
            SessionHelper.setCurrentUser(savedUserId);
            primaryStage.setTitle("Pinnacle");
            initialFxmlBaseName = "todolist";
        } else {
            primaryStage.setTitle("Pinnacle - Login");
            initialFxmlBaseName = "login";
        }

        root = loadFXML(initialFxmlBaseName);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        if (primaryStage.getStyle() == StageStyle.UNDECORATED && root != null) {
            makeDraggable(root, primaryStage);
        }

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
        });
    }

    public static <T> void navigateToViewAndPassQuery(
            String fxmlFileNameWithExtension,
            String title,
            boolean isResizable,
            String searchQuery,
            Class<T> expectedControllerClass
    ) throws IOException {

        String fxmlPath = FXML_PATH_PREFIX + fxmlFileNameWithExtension;
        URL fxmlUrl = ToDoListApplication.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("navigateToViewAndPassQuery: FXML file not found at " + fxmlPath);
            throw new IOException("FXML file not found: " + fxmlPath);
        }
        System.out.println("[ToDoListApplication] Navigating to: " + fxmlUrl.toExternalForm());

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Object controller = loader.getController();

        if (expectedControllerClass.isInstance(controller) && controller instanceof SearchableController) {
            ((SearchableController) controller).initializeWithSearchQuery(searchQuery);
            System.out.println("Query '" + searchQuery + "' diteruskan ke controller " + controller.getClass().getSimpleName());
        } else {
            System.err.println("Peringatan: Controller untuk " + fxmlFileNameWithExtension +
                    " (" + (controller != null ? controller.getClass().getName() : "null") + ")" +
                    " bukan instance dari " + expectedControllerClass.getName() +
                    " atau tidak mengimplementasikan SearchableController.");
        }

        if (primaryStage == null) { return; }
        primaryStage.getScene().setRoot(root);

        if (primaryStage.getStyle() == StageStyle.UNDECORATED && root != null) {
            makeDraggable(root, primaryStage);
        }
        primaryStage.sizeToScene();
        primaryStage.setResizable(isResizable);
        if (title != null) primaryStage.setTitle(title);
        primaryStage.centerOnScreen();
    }

    private static void makeDraggable(Parent rootNode, Stage stage) {
        rootNode.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        rootNode.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    private static Parent loadFXML(String fxmlBaseName) throws IOException {
        String fullPath = FXML_PATH_PREFIX + fxmlBaseName + ".fxml";
        System.out.println("[ToDoListApplication] loadFXML: Mencoba memuat FXML dari path: " + fullPath);

        URL fxmlLocation = ToDoListApplication.class.getResource(fullPath);
        if (fxmlLocation == null) {
            System.err.println("FATAL: File FXML tidak ditemukan pada path (loadFXML): " + fullPath);
            throw new IOException("File FXML tidak ditemukan pada path: " + fullPath);
        }
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        return fxmlLoader.load();
    }

    public static void setRoot(String fxmlBaseName, String title, boolean isResizable) throws IOException {
        System.out.println("[ToDoListApplication] setRoot dipanggil untuk: " + fxmlBaseName);
        Parent root = loadFXML(fxmlBaseName);

        if (primaryStage == null) {
            System.err.println("Error di setRoot: primaryStage adalah null.");
            return;
        }
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        } else {
            primaryStage.getScene().setRoot(root);
        }

        if (primaryStage.getStyle() == StageStyle.UNDECORATED && root != null) {
            makeDraggable(root, primaryStage);
        }
        primaryStage.sizeToScene();
        primaryStage.setResizable(isResizable);
        if (title != null) primaryStage.setTitle(title);
        primaryStage.centerOnScreen();
    }

    public static void openViewWithModal(String fxmlBaseName, boolean isResizeable) throws IOException {
        Stage modalStage = new Stage();
        modalStage.initStyle(StageStyle.UNDECORATED);
        try (InputStream iconStream = ToDoListApplication.class.getResourceAsStream("/icons/app_logo.png")) {
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                if (!appIcon.isError()) modalStage.getIcons().add(appIcon);
            }
        } catch (Exception e) { System.err.println("Exception ikon modal: " + e.getMessage()); }

        Parent root = loadFXML(fxmlBaseName);
        modalStage.setScene(new Scene(root));
        if (modalStage.getStyle() == StageStyle.UNDECORATED && root != null) {
            makeDraggable(root, modalStage);
        }
        modalStage.sizeToScene();
        modalStage.setResizable(isResizeable);
        modalStage.initOwner(primaryStage);
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
