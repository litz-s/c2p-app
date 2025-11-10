package com.example.c2p.store;

import com.example.c2p.util.ConfigPaths;
import com.example.c2p.model.CopyTable; // ← プロジェクトの実型に合わせて import 調整
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

// 先頭の import 群に追記
import java.util.concurrent.CompletableFuture;

public class Store {
    private final Path jsonPath;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<CopyTable> tables = new ArrayList<>();

    public Store(Path jsonPath) {
        this.jsonPath = jsonPath;
    }

    /** 既定の保存先で作るヘルパー。 */
    public static Store createDefault() {
        return new Store(ConfigPaths.defaultStorePath());
    }

    /** 読み込み。無ければ作成、壊れていれば .bak へ退避して初期化。 */
    public void load() {
        try {
            Files.createDirectories(jsonPath.getParent());
            if (!Files.exists(jsonPath)) {
                // 初回：空の配列で新規作成
                save();
                return;
            }
            byte[] bytes = Files.readAllBytes(jsonPath);
            List<CopyTable> loaded = mapper.readValue(bytes, new TypeReference<List<CopyTable>>() {
            });
            tables.clear();
            tables.addAll(loaded);
        } catch (IOException e) {
            // 壊れている等：バックアップして初期化
            try {
                Files.createDirectories(jsonPath.getParent());
                if (Files.exists(jsonPath)) {
                    Path bak = jsonPath.resolveSibling(jsonPath.getFileName() + ".bak");
                    Files.move(jsonPath, bak, StandardCopyOption.REPLACE_EXISTING);
                }
                tables.clear();
                save();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to recover store: " + jsonPath, ex);
            }
        }
    }

    // クラス内のどこでも良い（public メソッド群のあたり）に追記

    /** 非同期保存（UIスレッドをブロックしない） */
    public void saveAsync() {
        // 失敗時は RuntimeException を投げて終わらせるだけの簡易版
        CompletableFuture.runAsync(this::save);
    }

    /** IDでテーブルを1件検索。見つからなければ null を返す */
    public CopyTable findTableById(String id) {
        if (id == null)
            return null;
        for (CopyTable t : tables) {
            // プロジェクト側のID型/ゲッタ名に合わせてここだけ必要なら調整
            if (id.equals(t.getId())) {
                return t;
            }
        }
        return null;
    }

    /** 保存。 */
    public void save() {
        try {
            Files.createDirectories(jsonPath.getParent());
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tables);
            Files.writeString(
                    jsonPath,
                    json,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write store: " + jsonPath, e);
        }
    }

    // アクセサ
    public List<CopyTable> getTables() {
        return tables;
    }

    public Path getPath() {
        return jsonPath;
    }
}
