package com.sribalafashion.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    /**
     * Sync endpoint: called after Google OAuth login.
     * The Supabase JWT is ALREADY verified by JwtAuthenticationFilter,
     * so we just extract the email from the security context (no extra API call).
     */
    @PostMapping("/google")
    public ResponseEntity<?> syncGoogleUser(Authentication authentication,
            @RequestBody GoogleAuthRequest request) {
        // 1. JWT is already verified by JwtAuthenticationFilter — use security context
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("Not authenticated"));
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Could not extract email from token"));
        }

        // 2. Extract full name from request body (frontend sends it)
        String fullName = request.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = email.split("@")[0];
        }

        // 3. Find or create user — single DB call
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .role(User.Role.CUSTOMER)
                    .build();
            userRepository.save(user);
        }

        // 4. Return user info
        return ResponseEntity.ok(new UserInfoResponse(
                user.getEmail(),
                user.getRole().name(),
                user.getFullName()));
    }

    /**
     * Returns the current authenticated user's info.
     * The Supabase JWT is verified by the filter; this just looks up the DB record.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("Not authenticated"));
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(new UserInfoResponse(
                        user.getEmail(),
                        user.getRole().name(),
                        user.getFullName())))
                .orElse(ResponseEntity.notFound().build());
    }
}
