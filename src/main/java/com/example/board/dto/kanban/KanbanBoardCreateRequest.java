// src/main/java/com/example/board/dto/kanban/KanbanBoardCreateRequest.java
package com.example.board.dto.kanban;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanBoardCreateRequest {

    @NotNull(message = "팀 ID는 필수입니다")
    private Long teamId;

    @NotBlank(message = "보드 이름은 필수입니다")
    @Size(max = 100, message = "보드 이름은 100자 이하여야 합니다")
    private String name;

    @Size(max = 1000, message = "보드 설명은 1000자 이하여야 합니다")
    private String description;
}