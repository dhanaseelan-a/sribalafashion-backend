package com.sribalafashion.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final SupabaseAuthService supabaseAuthService;

    /**
     * Sync endpoint: called after Google OAuth login.
     * The Supabase JWT is already verified by the JwtAuthenticationFilter.
     * This endpoint ensures the user exists in our database and returns their info.
     */
    @PostMapping("/google")
    public ResponseEntity<?> syncGoogleUser(@RequestBody GoogleAuthRequest request) {
        // 1. Verify the Supabase access token via Supabase API (as extra validation)
        Map<String, Object> userInfo = supabaseAuthService.verifyToken(request.getAccessToken());
        if (userInfo == null) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("Invalid or expired token"));
        }

        // 2. Extract email from verified user info
        String email = supabaseAuthService.extractEmail(userInfo);
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Could not extract email from token"));
        }

        // 3. Extract full name
        String fullName = supabaseAuthService.extractFullName(userInfo);
        if (fullName == null || fullName.isBlank()) {
            fullName = email.split("@")[0];
        }

        // 4. Find or create user
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

        // 5. Return user info (no custom JWT — frontend uses Supabase JWT directly)
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
