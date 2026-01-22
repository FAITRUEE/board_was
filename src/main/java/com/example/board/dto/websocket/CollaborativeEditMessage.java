package com.example.board.dto.websocket;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborativeEditMessage {
    private Long postId;
    private Long userId;
    private String username;
    private MessageType type;
    private String content;
    private Integer cursorPosition;
    private Long timestamp;

    public enum MessageType {
        JOIN,           // 편집 세션 참여
        LEAVE,          // 편집 세션 나감
        CONTENT_CHANGE, // 내용 변경
        CURSOR_MOVE,    // 커서 이동
        SAVE            // 저장
    }
}
