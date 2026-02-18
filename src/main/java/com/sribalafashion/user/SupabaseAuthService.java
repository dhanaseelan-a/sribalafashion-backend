package com.sribalafashion.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SupabaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Verifies a Supabase access token by calling the Supabase Auth API.
     * Returns the user info map if valid, null if invalid.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("apikey", getApiKey());

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    supabaseUrl + "/auth/v1/user",
                    HttpMethod.GET,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> body = response.getBody();
            if (response.getStatusCode() == HttpStatus.OK && body != null) {
                logger.info("Supabase token verified for user: {}", body.get("email"));
                return body;
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to verify Supabase token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract email from the Supabase user info.
     */
    public String extractEmail(Map<String, Object> userInfo) {
        Object email = userInfo.get("email");
        return email != null ? email.toString() : null;
    }

    /**
     * Extract full name from Supabase user_metadata.
     */
    @SuppressWarnings("unchecked")
    public String extractFullName(Map<String, Object> userInfo) {
        try {
            Object metaObj = userInfo.get("user_metadata");
            if (metaObj instanceof Map) {
                Map<String, Object> metadata = (Map<String, Object>) metaObj;
                Object fullName = metadata.get("full_name");
                if (fullName == null)
                    fullName = metadata.get("name");
                return fullName != null ? fullName.toString() : null;
            }
        } catch (Exception e) {
            logger.error("Failed to extract full name", e);
        }
        return null;
    }

    private String getApiKey() {
        // The publishable anon key — same one used in the frontend
        return "sb_publishable_pON7nhFgiOn2f8QT2ttPKA_yXknLhey";
    }
}
