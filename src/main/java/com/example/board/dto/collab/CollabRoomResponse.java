package com.example.board.dto.collab;

import com.example.board.entity.CollabRoom;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollabRoomResponse {

    private Long id;
    private Long teamId;
    private String teamName;
    private Long createdById;
    private String createdByUsername;
    private String title;
    private String content;
    private Boolean isPublished;
    private Long publishedPostId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CollabRoomResponse from(CollabRoom room) {
        return CollabRoomResponse.builder()
                .id(room.getId())
                .teamId(room.getTeam().getId())
                .teamName(room.getTeam().getName())
                .createdById(room.getCreatedBy().getId())
                .createdByUsername(room.getCreatedBy().getUsername())
                .title(room.getTitle())
                .content(room.getContent())
                .isPublished(room.getIsPublished())
                .publishedPostId(room.getPublishedPostId())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
