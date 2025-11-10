package com.example.c2p.dialog;

import com.example.c2p.App;
import com.example.c2p.model.CopyTable;
import com.example.c2p.store.Store;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class EditTableDialog extends Dialog<Boolean> {

    public EditTableDialog() {
        this(null);
    }

    public EditTableDialog(String presetId) {
        setTitle("コピペテーブルの編集");
        setHeaderText("編集するテーブルを選択");
        ButtonType apply = new ButtonType("適用", ButtonBar.ButtonData.OK_DONE);
        ButtonType delete = new ButtonType("削除", ButtonBar.ButtonData.LEFT);

        getDialogPane().getButtonTypes().addAll(apply, delete, ButtonType.CANCEL);

        Store store = App.getStore();
        ComboBox<CopyTable> selector = new ComboBox<>();
        selector.getItems().addAll(store.getTables());
        selector.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CopyTable object) { return object == null ? "" : object.getName(); }
            @Override public CopyTable fromString(String string) { return null; }
        });
        if (presetId != null) {
            for (CopyTable t : store.getTables()) {
                if (t.getId().equals(presetId)) { selector.getSelectionModel().select(t); break; }
            }
        } else if (!store.getTables().isEmpty()) {
            selector.getSelectionModel().selectFirst();
        }

        TextField name = new TextField();
        selector.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                name.setText(n.getName());
            } else {
                name.clear();
            }
        });
        if (selector.getValue() != null) {
            name.setText(selector.getValue().getName());
        }

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(10));
        gp.add(new Label("対象"), 0, 0);
        gp.add(selector, 1, 0);
        gp.add(new Label("名前"), 0, 1);
        gp.add(name, 1, 1);
        getDialogPane().setContent(gp);

        setResultConverter(bt -> {
            CopyTable sel = selector.getValue();
            if (sel == null) return false;
            if (bt == apply) {
                sel.setName(name.getText().trim());
                return true;
            } else if (bt == delete) {
                store.getTables().remove(sel);
                return true;
            }
            return false;
        });
    }
}
