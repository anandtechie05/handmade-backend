package com.handmade.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.handmade.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "HandmadeBusinessSecretKeyForJwtAuthentication2026Secure";

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60 * 24;

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(User user) {

        Date now = new Date();

        Date expiration = new Date(
                now.getTime() + EXPIRATION_TIME
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public boolean isTokenValid(String token, String email) {

        Claims claims = extractAllClaims(token);

        String tokenEmail = claims.getSubject();

        Date expiration = claims.getExpiration();

        return tokenEmail.equals(email)
                && expiration.after(new Date());
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractRole(String token) {

    return extractAllClaims(token)
            .get("role", String.class);
}
}