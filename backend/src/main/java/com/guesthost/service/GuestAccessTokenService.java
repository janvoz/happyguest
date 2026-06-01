package com.guesthost.service;

import com.guesthost.model.Booking;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class GuestAccessTokenService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public GuestAccessTokenService(@Value("${app.jwt.secret}") String jwtSecret,
                                   @Value("${app.guest-access.expiration-ms:1209600000}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generatePortalToken(Booking booking) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(booking.getId())
                .claim("propertyId", booking.getPropertyId())
                .claim("type", "GUEST_PORTAL")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    public Optional<String> resolveBookingId(String propertyId, String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            String tokenType = claims.get("type", String.class);
            String tokenPropertyId = claims.get("propertyId", String.class);
            if (!"GUEST_PORTAL".equals(tokenType) || tokenPropertyId == null || !tokenPropertyId.equals(propertyId)) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
