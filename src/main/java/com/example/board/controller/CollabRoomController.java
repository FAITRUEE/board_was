package com.example.board.controller;

import com.example.board.dto.collab.CollabRoomContentRequest;
import com.example.board.dto.collab.CollabRoomCreateRequest;
import com.example.board.dto.collab.CollabRoomPublishRequest;
import com.example.board.dto.collab.CollabRoomResponse;
import com.example.board.security.UserPrincipal;
import com.example.board.service.CollabRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return ((UserPrincipal) auth.getPrincipal()).getId();
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

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        collabRoomService.deleteRoom(roomId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
