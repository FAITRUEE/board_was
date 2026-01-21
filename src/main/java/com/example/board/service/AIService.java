package com.example.board.service;

import com.example.board.dto.request.AIGenerationRequest;
import com.example.board.dto.response.AIGenerationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:exaone3.5:7.8b}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIGenerationResponse generatePost(AIGenerationRequest request) {
        try {
            String prompt = buildPrompt(request);

            // Ollama API 요청 구성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // API 호출
            String response = restTemplate.postForObject(
                    ollamaUrl + "/api/generate",
                    entity,
                    String.class
            );

            // 응답 파싱
            JsonNode jsonNode = objectMapper.readTree(response);
            String responseText = jsonNode.get("response").asText();

            System.out.println("=== Ollama 원본 응답 ===");
            System.out.println(responseText);

            // ✅ JSON 추출 및 정리
            int startIndex = responseText.indexOf("{");
            int endIndex = responseText.lastIndexOf("}") + 1;

            if (startIndex == -1 || endIndex <= startIndex) {
                throw new RuntimeException("AI 응답에서 JSON을 찾을 수 없습니다.");
            }

            String jsonString = responseText.substring(startIndex, endIndex);

            // ✅ 줄바꿈 문자 이스케이프 처리
            jsonString = jsonString
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            System.out.println("=== 정제된 JSON ===");
            System.out.println(jsonString);

            JsonNode resultNode = objectMapper.readTree(jsonString);

            return AIGenerationResponse.builder()
                    .title(resultNode.get("title").asText())
                    .content(resultNode.get("content").asText().replace("\\n", "\n"))  // ✅ 다시 줄바꿈으로 복원
                    .build();

        } catch (Exception e) {
            System.err.println("AI 생성 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI 생성 실패: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(AIGenerationRequest request) {
        Map<String, String> toneMap = Map.of(
                "friendly", "친근하고 따뜻한 말투로",
                "formal", "공식적이고 정중한 말투로",
                "humorous", "유머러스하고 재미있는 말투로",
                "professional", "전문적이고 신뢰감 있는 말투로"
        );

        Map<String, Integer> lengthMap = Map.of(
                "short", 300,
                "medium", 800,
                "long", 1500
        );

        Map<String, String> emojiMap = Map.of(
                "many", "이모지를 풍부하게 사용하여",
                "few", "적절한 이모지를 사용하여",
                "none", "이모지 없이"
        );

        return String.format("""
            당신은 게시글 작성을 도와주는 AI 어시스턴트입니다.
            
            다음 조건에 맞게 게시글을 작성해주세요:
            - 말투: %s
            - 길이: 약 %d자 내외
            - 이모지: %s
            - 언어: 한국어
            
            주제: %s
            
            **중요:** 응답은 반드시 아래 JSON 형식으로만 작성하세요. 
            JSON 내부의 content 필드에는 줄바꿈 대신 공백을 사용하세요.
            다른 설명은 포함하지 마세요.
            
            {"title": "게시글 제목 (50자 이내)", "content": "게시글 본문 내용 (한 줄로 작성)"}
            
            JSON만 응답하세요.
            """,
                toneMap.getOrDefault(request.getTone(), "친근하고 따뜻한 말투로"),
                lengthMap.getOrDefault(request.getLength(), 800),
                emojiMap.getOrDefault(request.getEmoji(), "적절한 이모지를 사용하여"),
                request.getPrompt()
        );
    }
}