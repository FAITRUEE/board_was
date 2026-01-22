// src/main/java/com/example/board/dto/kanban/KanbanCardUpdateRequest.java
package com.example.board.dto.kanban;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardUpdateRequest {

    @Size(max = 200, message = "카드 제목은 200자 이하여야 합니다")
    private String title;

    @Size(max = 5000, message = "카드 설명은 5000자 이하여야 합니다")
    private String description;

    private Long assignedTo;  // 담당자 ID

    private LocalDateTime dueDate;

    private String priority;
}