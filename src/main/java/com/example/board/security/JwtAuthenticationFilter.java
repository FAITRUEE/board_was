package com.example.board.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String bearerToken = request.getHeader("Authorization");

        // ✅ 디버깅 로그 추가
        System.out.println("========================================");
        System.out.println("🔍 Request: " + method + " " + requestURI);
        System.out.println("🔑 Authorization Header: " + bearerToken);

        String token = jwtTokenProvider.resolveToken(bearerToken);
        System.out.println("🔑 Resolved Token exists: " + (token != null));

        if (token != null && jwtTokenProvider.validateToken(token)) {
            System.out.println("🔑 Token preview: " + token.substring(0, Math.min(20, token.length())) + "...");

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            System.out.println("✅ Authentication SUCCESS for user ID: " + userId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ SecurityContext set successfully");
        } else if (token != null) {
            System.out.println("❌ Token validation FAILED");
        } else {
            System.out.println("⚠️  No token provided");
        }
        System.out.println("========================================");

        filterChain.doFilter(request, response);
    }
}