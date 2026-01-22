// src/main/java/com/example/board/dto/kanban/KanbanCardMoveRequest.java
package com.example.board.dto.kanban;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardMoveRequest {

    @NotBlank(message = "상태는 필수입니다")
    private String status;  // TODO, IN_PROGRESS, DONE

    @NotNull(message = "위치는 필수입니다")
    private Integer position;  // 새로운 위치
}