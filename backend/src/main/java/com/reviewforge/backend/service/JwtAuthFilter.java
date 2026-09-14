package com.reviewforge.backend.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class JwtAuthFilter extends OncePerRequestFilter {

    private JwtService jwtService;


    public void setJwtService(
            JwtService jwtService
    ) {
        this.jwtService = jwtService;
    }


    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path = request.getRequestURI();


        boolean isPublic =
                path.equals("/login")
                || path.equals("/users")
                || path.equals("/api/hello")
                || path.equals("/verify-security-code");


        return isPublic;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        System.out.println(
                "JWT FILTER EXECUTING"
        );


        String authHeader =
                request.getHeader(
                        "Authorization"
                );


        // ==========================================
        // CHECK AUTHORIZATION HEADER
        // ==========================================

        if (
                authHeader == null
                || authHeader.isBlank()
        ) {

            sendUnauthorized(
                    response,
                    "Authentication required"
            );

            return;
        }


        // ==========================================
        // CLEAN TOKEN
        // ==========================================

        String token =
                authHeader

                        .replaceFirst(
                                "(?i)^Bearer\\s+",
                                ""
                        )

                        .replaceAll(
                                "\\s+",
                                ""
                        )

                        .trim();


        if (token.isEmpty()) {

            sendUnauthorized(
                    response,
                    "Invalid token"
            );

            return;
        }


        try {

            // ======================================
            // EXTRACT EMAIL FROM JWT
            // ======================================

            String email =
                    jwtService.extractEmail(
                            token
                    );


            System.out.println(
                    "JWT valid for: " + email
            );


            // ======================================
            // STORE USER EMAIL IN REQUEST
            // ======================================

            request.setAttribute(
                    "userEmail",
                    email
            );


            // ======================================
            // CONTINUE REQUEST
            // ======================================

            filterChain.doFilter(
                    request,
                    response
            );


        } catch (Exception e) {

            System.out.println(
                    "JWT validation failed: "
                            + e.getMessage()
            );


            sendUnauthorized(
                    response,
                    "Invalid or expired token"
            );
        }
    }


    // ==========================================
    // SEND UNAUTHORIZED RESPONSE
    // ==========================================

    private void sendUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );


        response.setContentType(
                "application/json"
        );


        response.getWriter().write(
                "{\"error\":\""
                        + message
                        + "\"}"
        );
    }
}