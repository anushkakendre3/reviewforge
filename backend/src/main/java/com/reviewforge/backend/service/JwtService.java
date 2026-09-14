package com.reviewforge.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class JwtService {

    // ==========================================
    // JWT SECRET KEY
    // ==========================================

    private static final String SECRET_KEY =
            "reviewforge-secret-key-for-jwt-token-generation-123456";


    // ==========================================
    // TOKEN EXPIRATION
    // ==========================================

    private static final long EXPIRATION_TIME =
            1000 * 60 * 60;


    // ==========================================
    // GET SIGNING KEY
    // ==========================================

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }


    // ==========================================
    // GENERATE LOGIN TOKEN
    // SECURITY CODE NOT VERIFIED
    // ==========================================

    public String generateToken(
            String email
    ) {

        return Jwts.builder()

                .subject(
                        email.trim()
                )

                .claim(
                        "access_verified",
                        false
                )

                .issuedAt(
                        new Date()
                )

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                .signWith(
                        getSigningKey()
                )

                .compact();
    }


    // ==========================================
    // GENERATE VERIFIED TOKEN
    // SECURITY CODE VERIFIED
    // ==========================================

    public String generateVerifiedToken(
            String email
    ) {

        return Jwts.builder()

                .subject(
                        email.trim()
                )

                .claim(
                        "access_verified",
                        true
                )

                .issuedAt(
                        new Date()
                )

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                .signWith(
                        getSigningKey()
                )

                .compact();
    }


    // ==========================================
    // EXTRACT ALL CLAIMS
    // ==========================================

    private Claims extractClaims(
            String token
    ) {

        if (
                token == null
                || token.isBlank()
        ) {

            throw new RuntimeException(
                    "JWT token is empty"
            );
        }


        String cleanToken = token

                .replaceFirst(
                        "(?i)^Bearer\\s+",
                        ""
                )

                .replaceAll(
                        "\\s+",
                        ""
                )

                .trim();


        return Jwts.parser()

                .verifyWith(
                        getSigningKey()
                )

                .build()

                .parseSignedClaims(
                        cleanToken
                )

                .getPayload();
    }


    // ==========================================
    // EXTRACT EMAIL
    // ==========================================

    public String extractEmail(
            String token
    ) {

        Claims claims = extractClaims(
                token
        );

        return claims.getSubject();
    }


    // ==========================================
    // CHECK SECURITY CODE VERIFICATION
    // ==========================================

    public boolean isAccessVerified(
            String token
    ) {

        Claims claims = extractClaims(
                token
        );

        Boolean accessVerified = claims.get(
                "access_verified",
                Boolean.class
        );

        return Boolean.TRUE.equals(
                accessVerified
        );
    }
}