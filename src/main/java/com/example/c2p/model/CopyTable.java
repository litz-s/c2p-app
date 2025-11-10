package com.example.c2p.model;

import java.util.ArrayList;
import java.util.List;

public class CopyTable {
    private String id;
    private String name;
    private List<SnippetItem> items = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<SnippetItem> getItems() { return items; }
    public void setItems(List<SnippetItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "CopyTable{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", items=" + items.size() +
                '}';
    }
}
