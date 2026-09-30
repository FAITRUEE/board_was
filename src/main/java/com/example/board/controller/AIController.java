package com.example.board.controller;

import com.example.board.dto.request.AIGenerationRequest;
import com.example.board.dto.response.AIGenerationResponse;
import com.example.board.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;

    // ✅ 테스트 엔드포인트 추가
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AI Controller is working!");
    }

    @PostMapping("/generate")
    public ResponseEntity<AIGenerationResponse> generatePost(
            @RequestBody AIGenerationRequest request,
            Authentication authentication) {

        try {
            return ResponseEntity.ok(aiService.generatePost(request));
        } catch (Exception e) {
            log.error("AI 생성 실패", e);
            throw new RuntimeException("AI 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }
}