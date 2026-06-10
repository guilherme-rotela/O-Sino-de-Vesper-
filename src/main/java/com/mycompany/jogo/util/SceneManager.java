package com.mycompany.jogo.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/com/sinodevesper/fxml/" + fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneManager.class.getResource("/com/sinodevesper/css/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T navigateToWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/com/sinodevesper/fxml/" + fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneManager.class.getResource("/com/sinodevesper/css/style.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.show();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void openNewWindow(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/com/sinodevesper/fxml/" + fxmlPath)
            );
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneManager.class.getResource("/com/sinodevesper/css/style.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

