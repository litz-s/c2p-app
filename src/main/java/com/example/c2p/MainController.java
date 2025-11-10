package com.example.c2p;

import java.util.List;

import com.example.c2p.dialog.CreateTableDialog;
import com.example.c2p.dialog.EditTableDialog;
import com.example.c2p.model.CopyTable;
import com.example.c2p.store.Store;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane; 
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class MainController {

    @FXML
    private Button addBtn;

    @FXML
    private Button editBtn;

    @FXML
    private FlowPane cardsPane;

    @FXML
    public void initialize() {
        refreshCards();

        addBtn.setOnAction(e -> {
            CreateTableDialog dlg = new CreateTableDialog();
            dlg.showAndWait().ifPresent(newTable -> {
                Store store = App.getStore();
                store.getTables().add(newTable);
                store.save();
                refreshCards();
            });
        });

        editBtn.setOnAction(e -> {
            if (App.getStore().getTables().isEmpty()) {
                return;
            }
            EditTableDialog dlg = new EditTableDialog();
            dlg.showAndWait().ifPresent(updated -> {
                App.getStore().save();
                refreshCards();
            });
        });
    }

    private void refreshCards() {
        cardsPane.getChildren().clear();
        List<CopyTable> tables = App.getStore().getTables();
        for (CopyTable t : tables) {
            VBox card = buildCard(t);
            cardsPane.getChildren().add(card);
        }
        editBtn.setDisable(tables.isEmpty());
    }

    private VBox buildCard(CopyTable table) {
        VBox v = new VBox(6);
        v.setPadding(new Insets(12));
        v.setPrefWidth(220);
        v.setMinHeight(100);
        v.setStyle(
                "-fx-background-color: -fx-background;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: -fx-box-border;" +
                        "-fx-border-width: 1;" +
                        "-fx-cursor: hand;");

        v.setPickOnBounds(true);

        Label name = new Label(table.getName());
        name.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");
        name.setMaxWidth(Double.MAX_VALUE);
        name.setAlignment(Pos.CENTER); 
        name.setTextAlignment(TextAlignment.CENTER);
        v.getChildren().add(name);

        v.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.PRIMARY) {
                App.showTable(table.getId());
            }
        });

        v.setOnContextMenuRequested(ev -> {
            EditTableDialog dlg = new EditTableDialog(table.getId());
            dlg.showAndWait().ifPresent(updated -> {
                App.getStore().save();
                refreshCards();
            });
        });

        return v;
    }
}
