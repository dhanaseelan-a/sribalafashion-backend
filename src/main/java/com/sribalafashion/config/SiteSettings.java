package com.sribalafashion.config;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "site_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettings {

    @Id
    private Long id = 1L; // single-row pattern

    @Column(nullable = false)
    private Boolean maintenanceMode = false;

    private Long maintenanceEndTime; // epoch millis, null = no timer
}
