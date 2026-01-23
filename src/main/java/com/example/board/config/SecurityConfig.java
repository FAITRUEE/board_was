package com.example.board.config;

import com.example.board.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅✅✅ OPTIONS 메서드는 모두 허용 (CORS preflight) - 최상단에 위치!
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ 에러 페이지
                        .requestMatchers("/", "/error").permitAll()

                        // ✅ WebSocket 엔드포인트
                        .requestMatchers("/ws/**").permitAll()

                        // ✅ 인증 API (회원가입, 로그인)
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ 첨부파일 다운로드
                        .requestMatchers(HttpMethod.GET, "/api/posts/attachments/**").permitAll()

                        // ✅ 게시글 - GET 요청은 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()

                        // ✅ 게시글 - 조회수, 비밀번호 확인은 누구나 가능
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/views").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/verify-password").permitAll()

                        // ✅ 게시글 - POST, PUT, DELETE는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

                        // ✅ 좋아요는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/like").authenticated()

                        // ✅ 카테고리 - GET은 모두 허용
                        .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // ✅ 카테고리 - POST, PUT, DELETE는 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/categories").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").authenticated()

                        // ✅ 태그 - 모두 허용
                        .requestMatchers("/api/tags/**").permitAll()

                        // ✅ 칸반 보드 - 모두 허용 (필요시 authenticated()로 변경)
                        .requestMatchers("/api/kanban/**").permitAll()

                        // ✅ 팀 API - 모두 허용
                        .requestMatchers("/api/teams/**").permitAll()

                        // ✅ 댓글 API - 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()

                        // ✅ 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발 환경에서는 localhost:3000 허용
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000"));

        // 모든 HTTP 메서드 허용 (PATCH 포함!)
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}