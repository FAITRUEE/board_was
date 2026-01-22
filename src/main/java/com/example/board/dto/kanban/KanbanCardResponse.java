package com.example.board.dto.kanban;

import com.example.board.entity.KanbanCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardResponse {

    private Long id;
    private Long boardId;
    private String title;
    private String description;
    private String status;
    private Integer position;
    private Long assignedToId;
    private String assignedToUsername;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime dueDate; // ✅ 마감일
    private String priority; // ✅ 우선순위
    private Integer commentCount;
    private Integer checklistTotal; // ✅ 체크리스트 총 개수
    private Integer checklistCompleted; // ✅ 체크리스트 완료 개수
    private List<ChecklistItemResponse> checklistItems; // ✅ 체크리스트
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KanbanCardResponse from(KanbanCard card) {
        return KanbanCardResponse.builder()
                .id(card.getId())
                .boardId(card.getBoard().getId())
                .title(card.getTitle())
                .description(card.getDescription())
                .status(card.getStatus().name())
                .position(card.getPosition())
                .assignedToId(card.getAssignedTo() != null ? card.getAssignedTo().getId() : null)
                .assignedToUsername(card.getAssignedTo() != null ? card.getAssignedTo().getUsername() : null)
                .createdById(card.getCreatedBy().getId())
                .createdByUsername(card.getCreatedBy().getUsername())
                .dueDate(card.getDueDate()) // ✅
                .priority(card.getPriority().name()) // ✅
                .commentCount(card.getComments() != null ? card.getComments().size() : 0)
                .checklistTotal(card.getChecklistItems() != null ? card.getChecklistItems().size() : 0) // ✅
                .checklistCompleted(card.getChecklistItems() != null ?
                        (int) card.getChecklistItems().stream().filter(item -> item.getCompleted()).count() : 0) // ✅
                .checklistItems(card.getChecklistItems() != null ?
                        card.getChecklistItems().stream()
                                .map(ChecklistItemResponse::from)
                                .collect(Collectors.toList()) : null) // ✅
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChecklistItemResponse {
        private Long id;
        private String text;
        private Boolean completed;
        private Integer position;

        public static ChecklistItemResponse from(com.example.board.entity.KanbanChecklistItem item) {
            return ChecklistItemResponse.builder()
                    .id(item.getId())
                    .text(item.getText())
                    .completed(item.getCompleted())
                    .position(item.getPosition())
                    .build();
        }
    }
}