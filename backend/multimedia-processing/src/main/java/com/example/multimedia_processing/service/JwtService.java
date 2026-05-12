package com.example.multimedia_processing.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UUID userId, String email) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

//    public UUID extractUserId(String token) {
//
//        String userId = Jwts.parser()
//                .verifyWith((SecretKey) key)
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .get("userId", String.class);
//
//        return UUID.fromString(userId);
//    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String email = extractEmail(token);
        return email.equals(userDetails.getUsername());
    }
}