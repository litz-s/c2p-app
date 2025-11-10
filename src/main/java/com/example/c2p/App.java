package com.example.c2p;

import java.io.IOException;

import com.example.c2p.store.Store;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;
    private static Store store;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        store = new Store(com.example.c2p.util.ConfigPaths.defaultStorePath());
        store.load();

        showMain();
        stage.setTitle("C2P - Copy/Paste Tables");
        stage.show();
    }

    public static Store getStore() {
        return store;
    }

    public static void showMain() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/c2p/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 320);
            scene.getStylesheets().add(App.class.getResource("app.css").toExternalForm()); // ← 追加
            primaryStage.setScene(scene);
            applyAppIcons(primaryStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showTable(String tableId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/example/c2p/table.fxml"));
            Parent root = loader.load();
            TableController controller = loader.getController();
            controller.loadTable(tableId);
            Scene scene = new Scene(root, 550, 320);
            scene.getStylesheets().add(App.class.getResource("app.css").toExternalForm()); // ← 追加
            primaryStage.setScene(scene);
            applyAppIcons(primaryStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyAppIcons(javafx.stage.Window w) {
        if (!(w instanceof javafx.stage.Stage))
            return;
        var stage = (javafx.stage.Stage) w;
        var names = new String[] {
                "/icons/app-16.png", "/icons/app-32.png", "/icons/app-48.png",
                "/icons/app-64.png", "/icons/app-128.png", "/icons/app-256.png"
        };
        var imgs = new java.util.ArrayList<javafx.scene.image.Image>();
        for (var n : names) {
            var url = App.class.getResource(n);
            if (url != null)
                imgs.add(new javafx.scene.image.Image(url.toExternalForm()));
        }
        if (!imgs.isEmpty())
            stage.getIcons().setAll(imgs);
    }

    public static void main(String[] args) {
        launch();
    }
}
