package com.example.board.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIGenerationRequest {
    private String prompt;
    private String tone;      // friendly, formal, humorous, professional
    private String length;    // short, medium, long
    private String emoji;     // many, few, none
}