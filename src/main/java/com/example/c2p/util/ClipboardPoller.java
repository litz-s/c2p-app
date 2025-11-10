package com.example.c2p.util;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ClipboardPoller {
    private final Consumer<String> onNewText;
    private final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "clipboard-poller");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean running = false;
    private String last = null;

    public ClipboardPoller(Consumer<String> onNewText) {
        this.onNewText = onNewText;
    }

    public void start() {
        if (running) return;
        running = true;
        ses.scheduleAtFixedRate(() -> {
            if (!running) return;
            try {
                String now = Clipboard.getSystemClipboard().hasString() ? Clipboard.getSystemClipboard().getString() : null;
                if (now != null && !Objects.equals(now, last)) {
                    last = now;
                    Platform.runLater(() -> onNewText.accept(now));
                }
            } catch (Exception ignored) { }
        }, 0, 300, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        running = false;
        ses.shutdownNow();
    }
}
