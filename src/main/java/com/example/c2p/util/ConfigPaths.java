package com.example.c2p.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ConfigPaths {
    private ConfigPaths(){}

    public static Path defaultStorePath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            Path base = (appdata != null) ? Paths.get(appdata) : Paths.get(System.getProperty("user.home"), "AppData", "Roaming");
            return base.resolve("C2P").resolve("c2p.json");
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "C2P", "c2p.json");
        } else {
            return Paths.get(System.getProperty("user.home"), ".config", "C2P", "c2p.json");
        }
    }
}