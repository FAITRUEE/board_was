package com.example.board.dto.websocket;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborativeEditMessage {
    private Long postId;        // 게시글 ID 또는 공동 편집 방 ID
    private Long userId;
    private String username;
    private MessageType type;
    private String content;
    private Integer cursorPosition;
    private Long timestamp;
    private List<Editor> editors; // JOIN / LEAVE 시 서버가 채워주는 현재 편집자 목록

    public enum MessageType {
        JOIN,           // 편집 세션 참여
        LEAVE,          // 편집 세션 나감
        CONTENT_CHANGE, // 내용 변경
        CURSOR_MOVE,    // 커서 이동
        SAVE            // 저장
    }

    @Getter
    @AllArgsConstructor
    public static class Editor {
        private Long userId;
        private String username;
    }
}
