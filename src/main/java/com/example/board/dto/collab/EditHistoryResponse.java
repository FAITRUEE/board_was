package com.example.board.dto.collab;

import com.example.board.entity.EditHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditHistoryResponse {

    private Long id;
    private Long roomId;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private String changeDescription;
    private LocalDateTime createdAt;

    public static EditHistoryResponse from(EditHistory history) {
        return EditHistoryResponse.builder()
                .id(history.getId())
                .roomId(history.getRoomId())
                .userId(history.getUser().getId())
                .username(history.getUser().getUsername())
                .title(history.getTitle())
                .content(history.getContentSnapshot())
                .changeDescription(history.getChangeDescription())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
