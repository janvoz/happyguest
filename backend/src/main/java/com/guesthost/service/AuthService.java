package com.guesthost.service;

import com.guesthost.dto.AuthRequest;
import com.guesthost.dto.AuthResponse;
import com.guesthost.dto.RegisterRequest;
import com.guesthost.model.User;
import com.guesthost.repository.UserRepository;
import com.guesthost.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .subscriptionTier(User.SubscriptionTier.FREE)
                .subscriptionStatus("ACTIVE")
                .billingProvider("STRIPE")
                .build();
        userRepository.save(user);
        return login(new AuthRequest() {{ setEmail(request.getEmail()); setPassword(request.getPassword()); }});
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .subscriptionTier(user.getSubscriptionTier())
                .build();
    }
}
