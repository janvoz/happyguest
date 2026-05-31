package com.guesthost.controller;

import com.guesthost.dto.UserProfileDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.User;
import com.guesthost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/host/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "name", user.getName() == null ? "" : user.getName(),
                "iban", user.getIban() == null ? "" : user.getIban(),
                "swift", user.getSwift() == null ? "" : user.getSwift()
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(Authentication authentication,
                                                             @RequestBody UserProfileDto dto) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (dto.getName() != null) {
            user.setName(dto.getName().trim());
        }
        if (dto.getIban() != null) {
            user.setIban(dto.getIban().replace(" ", "").toUpperCase().trim().isBlank() ? null : dto.getIban().trim());
        }
        if (dto.getSwift() != null) {
            user.setSwift(dto.getSwift().trim().isBlank() ? null : dto.getSwift().trim().toUpperCase());
        }
        userRepository.save(user);
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "name", user.getName() == null ? "" : user.getName(),
                "iban", user.getIban() == null ? "" : user.getIban(),
                "swift", user.getSwift() == null ? "" : user.getSwift()
        ));
    }
}
