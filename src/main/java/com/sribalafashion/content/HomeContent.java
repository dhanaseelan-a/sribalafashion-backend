package com.sribalafashion.content;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "home_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hero Section
    @Column(nullable = false)
    @Builder.Default
    private String heroTitle = "Welcome to Sri Bala Fashion";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String heroSubtitle = "Discover our stunning collection of covering jewellery, imitation bangles, fashion accessories & garlands — elegant designs at affordable prices.";

    // Promo Banner
    @Column(nullable = false)
    @Builder.Default
    private String promoTitle = "✨ Special Collection Available Now ✨";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String promoText = "Explore our latest covering jewellery arrivals – handcrafted with love and tradition.";

    @Column(nullable = false)
    @Builder.Default
    private String promoBtnText = "Explore Collection";

    // Features Section
    @Column(nullable = false)
    @Builder.Default
    private String featureTitle = "Why Choose Us";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String featureSubtitle = "Quality covering jewellery you can trust";

    // Footer
    @Column(columnDefinition = "VARCHAR(500)")
    @Builder.Default
    private String footerAddress = "123 Fashion Street, Chennai, India";

    @Column(columnDefinition = "VARCHAR(255)")
    @Builder.Default
    private String footerPhone = "+91 98765 43210";

    @Column(columnDefinition = "VARCHAR(255)")
    @Builder.Default
    private String footerEmail = "hello@sribalafashion.com";

    @Column(columnDefinition = "VARCHAR(500)")
    @Builder.Default
    private String footerInstagram = "#";

    @Column(columnDefinition = "VARCHAR(500)")
    @Builder.Default
    private String footerFacebook = "#";

    @Column(columnDefinition = "VARCHAR(500)")
    @Builder.Default
    private String footerTwitter = "#";

    @Column(columnDefinition = "VARCHAR(500)")
    @Builder.Default
    private String footerYoutube = "#";

    // Payment Settings
    @Column(columnDefinition = "VARCHAR(255)")
    @Builder.Default
    private String upiId = "dhanaseelan.a12345-3@okicici";
}
