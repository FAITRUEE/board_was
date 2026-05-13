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

        System.out.println("========================================");
        System.out.println("🔍 Request: " + method + " " + requestURI);
        System.out.println("🔑 Authorization Header: " + bearerToken);

        String token = jwtTokenProvider.resolveToken(bearerToken);
        System.out.println("🔑 Resolved Token exists: " + (token != null));

        if (token != null && jwtTokenProvider.validateToken(token)) {
            System.out.println("🔑 Token preview: " + token.substring(0, Math.min(20, token.length())) + "...");

            // ✅ JWT에서 사용자 정보 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            System.out.println("✅ Authentication SUCCESS for user ID: " + userId);

            // ✅ UserPrincipal 객체 생성
            UserPrincipal userPrincipal = UserPrincipal.builder()
                    .id(userId)
                    .email(email)
                    .username(username)
                    .build();

            // ✅ Authentication 생성 (principal을 UserPrincipal로 설정)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userPrincipal,  // ← 여기가 핵심!
                            null,
                            new ArrayList<>()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ SecurityContext set successfully with UserPrincipal");
        } else if (token != null) {
            System.out.println("❌ Token validation FAILED");
        } else {
            System.out.println("⚠️  No token provided");
        }
        System.out.println("========================================");

        filterChain.doFilter(request, response);
    }
}