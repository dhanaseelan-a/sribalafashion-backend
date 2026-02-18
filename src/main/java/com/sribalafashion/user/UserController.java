package com.sribalafashion.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserController {

        private final UserRepository userRepository;

        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> listUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<User> userPage = userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
                List<Map<String, Object>> content = userPage.getContent()
                                .stream()
                                .map(user -> Map.<String, Object>of(
                                                "id", user.getUserId(),
                                                "fullName", user.getFullName(),
                                                "email", user.getEmail(),
                                                "role", user.getRole().name(),
                                                "createdAt",
                                                user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""))
                                .collect(Collectors.toList());
                return ResponseEntity.ok(Map.of(
                                "content", content,
                                "totalPages", userPage.getTotalPages(),
                                "totalElements", userPage.getTotalElements(),
                                "currentPage", userPage.getNumber(),
                                "size", userPage.getSize()));
        }

        @PutMapping("/users/{id}/role")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                        @RequestBody Map<String, String> body,
                        Authentication authentication) {
                String currentEmail = authentication.getName();
                String newRole = body.get("role");
                if (newRole == null || (!newRole.equals("ADMIN") && !newRole.equals("CUSTOMER"))) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "Invalid role. Must be ADMIN or CUSTOMER"));
                }
                return userRepository.findById(id)
                                .map(user -> {
                                        if (user.getEmail().equals(currentEmail)) {
                                                return ResponseEntity.badRequest()
                                                                .body(Map.of("message",
                                                                                "You cannot change your own role"));
                                        }
                                        user.setRole(User.Role.valueOf(newRole));
                                        userRepository.save(user);
                                        return ResponseEntity.ok(Map.of("message", "Role updated to " + newRole));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }

        @DeleteMapping("/users/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
                // Prevent self-deletion
                String currentEmail = authentication.getName();
                return userRepository.findById(id)
                                .map(user -> {
                                        if (user.getEmail().equals(currentEmail)) {
                                                return ResponseEntity.badRequest()
                                                                .body(Map.of("message",
                                                                                "You cannot delete your own account"));
                                        }
                                        userRepository.delete(user);
                                        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
                                })
                                .orElse(ResponseEntity.notFound().build());
        }
}
