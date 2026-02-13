package com.carrental.bookingservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // ✅ Works for BOTH: "CUSTOMER" and "ROLE_CUSTOMER"
    public String extractRole(String token) {
        Claims claims = extractClaims(token);

        String role = claims.get("role", String.class);

        if (role == null || role.isBlank()) {
            return "ROLE_CUSTOMER";
        }

        role = role.trim().toUpperCase(); // ✅ MAIN FIX

        // ✅ If already ROLE_ format
        if (role.startsWith("ROLE_")) {
            return role;
        }

        return "ROLE_" + role;
    }


    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ JWT validateToken ERROR: " + e.getClass().getName() + " -> " + e.getMessage());
            return false;
        }
    }

}
