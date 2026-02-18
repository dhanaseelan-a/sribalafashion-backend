package com.sribalafashion.content;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HomeContentController {

    private final HomeContentRepository homeContentRepository;

    // Public — anyone can read home content
    @GetMapping("/content/home")
    public ResponseEntity<HomeContent> getHomeContent() {
        HomeContent content = homeContentRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> homeContentRepository.save(HomeContent.builder().build()));
        return ResponseEntity.ok(content);
    }

    // Admin only — update home content
    @PutMapping("/admin/content/home")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HomeContent> updateHomeContent(@RequestBody HomeContent updated) {
        HomeContent content = homeContentRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> homeContentRepository.save(HomeContent.builder().build()));

        content.setHeroTitle(updated.getHeroTitle());
        content.setHeroSubtitle(updated.getHeroSubtitle());
        content.setPromoTitle(updated.getPromoTitle());
        content.setPromoText(updated.getPromoText());
        content.setPromoBtnText(updated.getPromoBtnText());
        content.setFeatureTitle(updated.getFeatureTitle());
        content.setFeatureSubtitle(updated.getFeatureSubtitle());

        // Footer fields
        content.setFooterAddress(updated.getFooterAddress());
        content.setFooterPhone(updated.getFooterPhone());
        content.setFooterEmail(updated.getFooterEmail());
        content.setFooterInstagram(updated.getFooterInstagram());
        content.setFooterFacebook(updated.getFooterFacebook());
        content.setFooterTwitter(updated.getFooterTwitter());
        content.setFooterYoutube(updated.getFooterYoutube());

        // Payment Settings
        content.setUpiId(updated.getUpiId());

        return ResponseEntity.ok(homeContentRepository.save(content));
    }
}
