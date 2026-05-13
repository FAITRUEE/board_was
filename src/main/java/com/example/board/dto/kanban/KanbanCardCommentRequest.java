// src/main/java/com/example/board/dto/kanban/KanbanCardCommentRequest.java
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
public class KanbanCardCommentRequest {

    @NotBlank(message = "댓글 내용은 필수입니다")
    @Size(max = 1000, message = "댓글은 1000자 이하여야 합니다")
    private String content;
}