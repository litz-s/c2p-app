package com.example.c2p.store;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.c2p.model.CopyTable;
import com.example.c2p.util.ConfigPaths;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Store {
    private final Path jsonPath;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<CopyTable> tables = new ArrayList<>();

    public Store(Path jsonPath) {
        this.jsonPath = jsonPath;
    }

    public static Store createDefault() {
        return new Store(ConfigPaths.defaultStorePath());
    }

    public void load() {
        try {
            Files.createDirectories(jsonPath.getParent());
            if (!Files.exists(jsonPath)) {
                save();
                return;
            }
            byte[] bytes = Files.readAllBytes(jsonPath);
            List<CopyTable> loaded = mapper.readValue(bytes, new TypeReference<List<CopyTable>>() {
            });
            tables.clear();
            tables.addAll(loaded);
        } catch (IOException e) {
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

    /** 非同期保存（UIスレッドをブロックしない） */
    public void saveAsync() {
        CompletableFuture.runAsync(this::save);
    }

    public CopyTable findTableById(String id) {
        if (id == null)
            return null;
        for (CopyTable t : tables) {
            if (id.equals(t.getId())) {
                return t;
            }
        }
        return null;
    }

    /** 保存 */
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

    public List<CopyTable> getTables() {
        return tables;
    }

    public Path getPath() {
        return jsonPath;
    }
}
