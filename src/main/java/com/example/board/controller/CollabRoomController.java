package com.example.board.controller;

import com.example.board.dto.collab.CollabRoomContentRequest;
import com.example.board.dto.collab.CollabRoomCreateRequest;
import com.example.board.dto.collab.CollabRoomPublishRequest;
import com.example.board.dto.collab.CollabRoomResponse;
import com.example.board.dto.collab.EditHistoryResponse;
import com.example.board.dto.websocket.CollaborativeEditMessage;
import com.example.board.entity.EditSession.TargetType;
import com.example.board.security.UserPrincipal;
import com.example.board.service.CollabRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collab-rooms")
@RequiredArgsConstructor
public class CollabRoomController {

    private final CollabRoomService collabRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    private UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @GetMapping
    public ResponseEntity<List<CollabRoomResponse>> getMyRooms() {
        return ResponseEntity.ok(collabRoomService.getMyRooms(getCurrentUserId()));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<CollabRoomResponse> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(collabRoomService.getRoom(roomId, getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<CollabRoomResponse> createRoom(@RequestBody CollabRoomCreateRequest request) {
        return ResponseEntity.ok(collabRoomService.createRoom(getCurrentUserId(), request));
    }

    @PutMapping("/{roomId}/content")
    public ResponseEntity<CollabRoomResponse> updateContent(
            @PathVariable Long roomId,
            @RequestBody CollabRoomContentRequest request) {
        return ResponseEntity.ok(collabRoomService.updateContent(roomId, getCurrentUserId(), request));
    }

    @PostMapping("/{roomId}/publish")
    public ResponseEntity<Map<String, Long>> publish(
            @PathVariable Long roomId,
            @RequestBody CollabRoomPublishRequest request) {
        Long postId = collabRoomService.publishAsPost(roomId, getCurrentUserId(), request);
        return ResponseEntity.ok(Map.of("postId", postId));
    }

    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<EditHistoryResponse>> getHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(collabRoomService.getHistory(roomId, getCurrentUserId()));
    }

    @PostMapping("/{roomId}/history")
    public ResponseEntity<Void> createSnapshot(
            @PathVariable Long roomId,
            @RequestBody(required = false) Map<String, String> body) {
        String description = body != null ? body.get("description") : null;
        collabRoomService.createSnapshot(roomId, getCurrentUserId(), description);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/history/{historyId}/restore")
    public ResponseEntity<CollabRoomResponse> restoreSnapshot(
            @PathVariable Long roomId,
            @PathVariable Long historyId) {
        UserPrincipal user = getCurrentUser();
        CollabRoomResponse room = collabRoomService.restoreSnapshot(roomId, historyId, user.getId());

        // 방에 접속 중인 다른 편집자 화면에도 복원된 내용 반영
        messagingTemplate.convertAndSend(TargetType.COLLAB_ROOM.topic(roomId),
                CollaborativeEditMessage.builder()
                        .postId(roomId)
                        .userId(user.getId())
                        .username(user.getUsername())
                        .type(CollaborativeEditMessage.MessageType.CONTENT_CHANGE)
                        .content(room.getContent())
                        .timestamp(System.currentTimeMillis())
                        .build());

        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        collabRoomService.deleteRoom(roomId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
