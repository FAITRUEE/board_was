package com.example.board.controller;

import com.example.board.dto.response.CommentListResponse;
import com.example.board.dto.response.CommentResponse;
import com.example.board.dto.request.CreateCommentRequest;
import com.example.board.dto.request.UpdateCommentRequest;
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
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = commentService.createComment(userId, postId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = commentService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}