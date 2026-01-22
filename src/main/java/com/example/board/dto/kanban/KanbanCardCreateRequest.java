// src/main/java/com/example/board/dto/kanban/KanbanCardCreateRequest.java
package com.example.board.dto.kanban;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardCreateRequest {

    @NotBlank(message = "카드 제목은 필수입니다")
    @Size(max = 200, message = "카드 제목은 200자 이하여야 합니다")
    private String title;

    @Size(max = 5000, message = "카드 설명은 5000자 이하여야 합니다")
    private String description;

    private String status;  // TODO, IN_PROGRESS, DONE (기본값: TODO)

    private Long assignedTo;  // 담당자 ID (선택)
}