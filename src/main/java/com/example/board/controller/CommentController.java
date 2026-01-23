package com.example.board.controller;

import com.example.board.dto.response.CommentListResponse;
import com.example.board.dto.response.CommentResponse;
import com.example.board.dto.request.CreateCommentRequest;
import com.example.board.dto.request.UpdateCommentRequest;
import com.example.board.security.UserPrincipal;  // ✅ import 추가
import com.example.board.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ✅ userId 추출 헬퍼 메서드 추가
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }

        return null;
    }

    @GetMapping
    public ResponseEntity<CommentListResponse> getComments(@PathVariable Long postId) {
        CommentListResponse response = commentService.getComments(postId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);  // ✅ 수정

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        CommentResponse response = commentService.createComment(userId, postId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);  // ✅ 수정

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        CommentResponse response = commentService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);  // ✅ 수정

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}