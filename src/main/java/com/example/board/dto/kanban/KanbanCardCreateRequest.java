package com.example.board.dto.kanban;

import jakarta.validation.constraints.NotBlank;
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
public class KanbanCardCreateRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    private String description;

    private String status; // TODO, IN_PROGRESS, DONE

    private Long assignedTo; // 담당자 User ID

    private LocalDateTime dueDate; // ✅ 마감일

    private String priority; // ✅ 우선순위 (LOW, MEDIUM, HIGH, URGENT)
}