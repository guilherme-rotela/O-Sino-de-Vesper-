package com.mycompany.jogo.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static URL resolverFxml(String fxmlPath) {
        URL url = SceneManager.class.getResource("/com/mycompany/jogo/" + fxmlPath);
        if (url == null) {
            System.err.println("[SceneManager] FXML não encontrado: /com/mycompany/jogo/" + fxmlPath);
        }
        return url;
    }

    private static URL resolverCss() {
        return SceneManager.class.getResource("/com/mycompany/jogo/style.css");
    }

    public static void navigateTo(String fxmlPath) {
        try {
            URL fxmlUrl = resolverFxml(fxmlPath);
            if (fxmlUrl == null) return;

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            URL cssUrl = resolverCss();
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T navigateToWithController(String fxmlPath) {
        try {
            URL fxmlUrl = resolverFxml(fxmlPath);
            if (fxmlUrl == null) return null;

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            URL cssUrl = resolverCss();
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

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
            URL fxmlUrl = resolverFxml(fxmlPath);
            if (fxmlUrl == null) return;

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            URL cssUrl = resolverCss();
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}