package com.sribalafashion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SiteSettingsController {

    private final SiteSettingsRepository settingsRepository;

    private SiteSettings getOrCreate() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            SiteSettings s = new SiteSettings();
            s.setId(1L);
            s.setMaintenanceMode(false);
            s.setMaintenanceEndTime(null);
            return settingsRepository.save(s);
        });
    }

    // Public endpoint — anyone can check maintenance status
    @GetMapping("/maintenance")
    public ResponseEntity<?> getMaintenanceStatus() {
        SiteSettings settings = getOrCreate();
        // Auto-disable maintenance if timer expired
        if (settings.getMaintenanceMode() && settings.getMaintenanceEndTime() != null
                && System.currentTimeMillis() > settings.getMaintenanceEndTime()) {
            settings.setMaintenanceMode(false);
            settings.setMaintenanceEndTime(null);
            settingsRepository.save(settings);
        }
        long remaining = 0;
        if (settings.getMaintenanceMode() && settings.getMaintenanceEndTime() != null) {
            remaining = settings.getMaintenanceEndTime() - System.currentTimeMillis();
            if (remaining < 0)
                remaining = 0;
        }
        return ResponseEntity.ok(Map.of(
                "maintenanceMode", settings.getMaintenanceMode(),
                "maintenanceEndTime", settings.getMaintenanceEndTime() != null ? settings.getMaintenanceEndTime() : 0,
                "timeRemainingMs", remaining));
    }

    // Admin: toggle maintenance on/off
    @PostMapping("/maintenance/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleMaintenance() {
        SiteSettings settings = getOrCreate();
        settings.setMaintenanceMode(!settings.getMaintenanceMode());
        if (!settings.getMaintenanceMode()) {
            settings.setMaintenanceEndTime(null);
        }
        settingsRepository.save(settings);
        return ResponseEntity.ok(Map.of(
                "maintenanceMode", settings.getMaintenanceMode(),
                "message", settings.getMaintenanceMode() ? "Maintenance mode enabled" : "Maintenance mode disabled"));
    }

    // Admin: start maintenance with a timer (minutes)
    @PostMapping("/maintenance/timer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> startWithTimer(@RequestBody Map<String, Integer> body) {
        int minutes = body.getOrDefault("minutes", 0);
        if (minutes <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Minutes must be greater than 0"));
        }
        SiteSettings settings = getOrCreate();
        settings.setMaintenanceMode(true);
        settings.setMaintenanceEndTime(System.currentTimeMillis() + (long) minutes * 60 * 1000);
        settingsRepository.save(settings);
        return ResponseEntity.ok(Map.of(
                "maintenanceMode", true,
                "maintenanceEndTime", settings.getMaintenanceEndTime(),
                "message", "Maintenance started for " + minutes + " minutes"));
    }
}
