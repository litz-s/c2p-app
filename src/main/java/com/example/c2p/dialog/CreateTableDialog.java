package com.example.c2p.dialog;

import com.example.c2p.model.CopyTable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.UUID;

public class CreateTableDialog extends Dialog<CopyTable> {

    public CreateTableDialog() {
        setTitle("新しいコピペテーブル");
        setHeaderText("名前を入力");

        ButtonType ok = new ButtonType("作成", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("テーブル名");

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(10));
        gp.add(new Label("名前"), 0, 0);
        gp.add(nameField, 1, 0);

        Node okBtn = getDialogPane().lookupButton(ok);
        okBtn.setDisable(true);
        nameField.textProperty().addListener((obs, o, n) -> okBtn.setDisable(n == null || n.isBlank()));

        getDialogPane().setContent(gp);

        setResultConverter(bt -> {
            if (bt == ok) {
                CopyTable t = new CopyTable();
                t.setId(UUID.randomUUID().toString());
                t.setName(nameField.getText().trim());
                return t;
            }
            return null;
        });
    }
}
