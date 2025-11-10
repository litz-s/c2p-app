package com.example.c2p;

import com.example.c2p.model.CopyTable;
import com.example.c2p.model.SnippetItem;
import com.example.c2p.store.Store;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;
import javafx.beans.value.ChangeListener;

// 追加 import
import javafx.stage.Popup;
import javafx.geometry.Point2D;
import javafx.animation.FadeTransition;

import java.time.Instant;
import java.util.Objects;
import javafx.util.Duration;
import java.util.UUID;

import javafx.collections.transformation.FilteredList;
import java.util.Locale;

public class TableController {

    @FXML
    private BorderPane root;

    // Show the "Copied" popup near the given TextArea
    private void showCopiedPopupNear(TextArea ta) {
        if (copiedPopup.isShowing()) {
            copiedPopup.hide();
        }
        // Calculate position relative to the TextArea
        Point2D nodeCoord = ta.localToScene(0, 0);
        double x = ta.getScene().getWindow().getX() + nodeCoord.getX() + ta.getScene().getX();
        double y = ta.getScene().getWindow().getY() + nodeCoord.getY() + ta.getScene().getY() - 32; // show above

        copiedPopup.show(ta.getScene().getWindow(), x, y);

        // Fade out after 1 second
        FadeTransition ft = new FadeTransition(Duration.millis(800), copiedPopup.getContent().get(0));
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            copiedPopup.hide();
            copiedPopup.getContent().get(0).setOpacity(1.0); // reset for next time
        });
        ft.play();
    }

    @FXML
    private Label titleLabel;

    @FXML
    private Button backBtn;

    @FXML
    private Button addRowBtn;

    @FXML
    private ListView<SnippetItem> listView;

    @FXML
    private TextField searchField;
    @FXML
    private Button clearSearchBtn;

    private FilteredList<SnippetItem> filtered;
    private CopyTable table;
    private ObservableList<SnippetItem> items;

    @FXML
    public void initialize() {
        root.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.isControlDown() && ev.getCode() == KeyCode.F) {
                searchField.requestFocus();
                searchField.selectAll();
                ev.consume();
            } else if (ev.getCode() == KeyCode.F3) {
                // 次の一致へ（簡易版：今より下の最初の一致を探す）
                String q = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase(Locale.ROOT);
                if (!q.isEmpty()) {
                    int start = Math.max(0, listView.getSelectionModel().getSelectedIndex());
                    for (int i = start + 1; i < filtered.size(); i++) {
                        String t = String.valueOf(filtered.get(i).getText()).toLowerCase(Locale.ROOT);
                        if (t.contains(q)) {
                            listView.getSelectionModel().clearAndSelect(i);
                            listView.scrollTo(i);
                            break;
                        }
                    }
                    ev.consume();
                }
            }
        });
        
        backBtn.setOnAction(e -> {
            App.showMain();
        });
        addRowBtn.setOnAction(e -> addNewRow(""));
        setupListCellFactory();
    }

    private void setupListCellFactory() {
        listView.setCellFactory(lv -> new ListCell<>() {

            private final TextArea ta = new TextArea();
            private final Button deleteBtn = new Button("×");
            private final HBox root = new HBox(8);
            private final PauseTransition singleClickDelay = new PauseTransition(Duration.millis(220));

            {
                // TextArea 基本設定
                ta.setWrapText(true); // 折り返しON（内部スクロールしにくくする）
                ta.setEditable(false);
                ta.setPrefRowCount(1);
                ta.setMinHeight(Region.USE_PREF_SIZE); // Pref を下回らない
                ta.setMaxHeight(Double.MAX_VALUE);

                // 高さ自動更新関数
                ChangeListener<Object> autoHeight = (obs, o, n) -> updateAutoHeight();
                ta.textProperty().addListener(autoHeight);
                ta.widthProperty().addListener(autoHeight);

                // 初期表示時にも反映
                Platform.runLater(this::updateAutoHeight);

                // Tab 挿入（編集時のみ）
                ta.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
                    if (ta.isEditable() && ev.getCode() == KeyCode.TAB) {
                        int pos = ta.getCaretPosition();
                        ta.insertText(pos, "\t");
                        ev.consume();
                    }
                });

                // クリック判定：シングル/ダブルを厳密に分離
                ta.setOnMouseClicked(ev -> {
                    if (ev.getClickCount() == 2) {
                        // ダブルクリック：シングル処理をキャンセルして編集へ
                        singleClickDelay.stop();
                        ta.setEditable(true);
                        ta.requestFocus();
                        ta.positionCaret(ta.getText().length());
                    } else if (ev.getClickCount() == 1) {
                        singleClickDelay.setOnFinished(_e -> {
                            if (!ta.isEditable()) {
                                String text = ta.getText() == null ? "" : ta.getText();
                                ClipboardContent cc = new ClipboardContent();
                                cc.putString(text);
                                Clipboard.getSystemClipboard().setContent(cc);

                                // ---- ここからポップ表示 ----
                                showCopiedPopupNear(ta);
                                // ---- ここまで ----
                            }
                        });
                        singleClickDelay.playFromStart();
                    }
                });

                // フォーカスアウトで保存
                ta.focusedProperty().addListener((obs, was, is) -> {
                    if (!is && ta.isEditable()) {
                        ta.setEditable(false);
                        SnippetItem it = getItem();
                        if (it != null) {
                            it.setText(ta.getText());
                            it.setUpdatedAt(Instant.now().toString());
                            App.getStore().saveAsync();
                            // 編集により改行数が変わったら高さも更新
                            updateTextAreaRows();
                        }
                    }
                });

                // 削除ボタン
                deleteBtn.setFocusTraversable(false);
                deleteBtn.setOnAction(e -> {
                    SnippetItem it = getItem();
                    if (it == null)
                        return;
                    items.remove(it);
                    table.getItems().removeIf(x -> Objects.equals(x.getId(), it.getId()));
                    App.getStore().saveAsync();
                });
                deleteBtn.setPadding(new Insets(2, 8, 2, 8));
                deleteBtn.setStyle("-fx-background-radius: 8;");

                // セル全体の見た目
                root.getChildren().addAll(ta, deleteBtn);
                root.setPadding(new Insets(4));
                HBox.setHgrow(ta, Priority.ALWAYS);

                // ホバー時だけ削除ボタンを見せる（好みで常時表示でもOK）
                root.hoverProperty().addListener((o, was, is) -> deleteBtn.setVisible(is));
                deleteBtn.setVisible(false);
            }

            // 改行数に応じて TextArea の行数を調整
            private void updateTextAreaRows() {
                String txt = ta.getText() == null ? "" : ta.getText();
                int lines = Math.max(1, txt.split("\\R", -1).length);
                ta.setPrefRowCount(lines);
            }

            private void updateAutoHeight() {
                String txt = ta.getText();
                if (txt == null || txt.isEmpty())
                    txt = " "; // 空でも最低1行分確保

                double wrap = Math.max(0, ta.getWidth() - 16); // 適度に余白分を差し引く（必要に応じて調整）
                Text meas = new Text(txt);
                meas.setFont(ta.getFont());
                meas.setWrappingWidth(wrap);

                // Insets + 余裕分
                double pad = ta.getInsets().getTop() + ta.getInsets().getBottom() + 8;
                double h = meas.getLayoutBounds().getHeight() + pad;

                // 一行の最小高さも確保
                double oneLine = ta.getFont().getSize() + pad;
                ta.setPrefHeight(Math.max(h, oneLine));
            }

            @Override
            protected void updateItem(SnippetItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    ta.setText(Objects.toString(item.getText(), ""));
                    updateAutoHeight(); // 表示時にも高さ同期
                    setGraphic(root);
                }
            }
        });
    }

    public void loadTable(String tableId) {
        Store store = App.getStore();
        this.table = store.findTableById(tableId);
        if (this.table == null)
            throw new IllegalArgumentException("Table not found: " + tableId);
        titleLabel.setText(this.table.getName());

        this.items = FXCollections.observableArrayList(table.getItems());
        this.filtered = new FilteredList<>(items, it -> true);

        listView.setItems(filtered);

        // 検索テキストに応じて絞り込み（大小無視・部分一致）
        searchField.textProperty().addListener((obs, ov, nv) -> {
            final String q = nv == null ? "" : nv.trim().toLowerCase(Locale.ROOT);
            if (q.isEmpty()) {
                filtered.setPredicate(it -> true);
            } else {
                filtered.setPredicate(it -> {
                    String t = it.getText();
                    if (t == null)
                        return false;
                    return t.toLowerCase(Locale.ROOT).contains(q);
                });
            }
            // 絞り込み後に先頭へスクロール
            if (!filtered.isEmpty())
                listView.scrollTo(0);
        });

        // クリアボタン
        clearSearchBtn.setOnAction(e -> searchField.clear());
    }

    private void addNewRow(String text) {
        SnippetItem item = new SnippetItem();
        item.setId(UUID.randomUUID().toString());
        item.setText(text);
        item.setCreatedAt(Instant.now().toString());
        item.setUpdatedAt(Instant.now().toString());
        items.add(0, item);
        table.getItems().add(0, item);
        App.getStore().saveAsync();
        Platform.runLater(() -> listView.scrollTo(0));
    }

    // --- Copied ポップ ---
    private final Popup copiedPopup = new Popup();
    {
        Label chip = new Label("Copied");
        chip.setStyle("""
                    -fx-background-color: rgba(0,0,0,0.85);
                    -fx-text-fill: white;
                    -fx-padding: 6 10;
                    -fx-background-radius: 10;
                    -fx-font-size: 12;
                """);
        chip.setMouseTransparent(true);
        copiedPopup.getContent().add(chip);
        copiedPopup.setAutoHide(true);
    }
}