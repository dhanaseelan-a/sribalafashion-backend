package com.sribalafashion.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@RestController
public class KeepAliveController {

    /**
     * Lightweight keep-alive endpoint — NO database hit.
     * Returns server uptime and memory info so Render
     * treats it as real application activity.
     */
    @GetMapping("/api/keep-alive")
    public ResponseEntity<?> keepAlive() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeMinutes = uptimeMs / 60000;

        Runtime runtime = Runtime.getRuntime();
        long usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

        return ResponseEntity.ok(Map.of(
                "status", "alive",
                "timestamp", Instant.now().toString(),
                "uptimeMinutes", uptimeMinutes,
                "memoryUsedMB", usedMemoryMB));
    }
}
