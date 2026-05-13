package com.example.board.controller;

import com.example.board.dto.request.AIGenerationRequest;
import com.example.board.dto.response.AIGenerationResponse;
import com.example.board.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
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

        System.out.println("=== AI 생성 요청 받음 ===");
        System.out.println("Prompt: " + request.getPrompt());

        try {
            AIGenerationResponse response = aiService.generatePost(request);
            System.out.println("=== AI 생성 성공 ===");
            System.out.println("Title: " + response.getTitle());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("=== AI 생성 실패 ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();  // ← 이게 중요!
            throw new RuntimeException("AI 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }
}