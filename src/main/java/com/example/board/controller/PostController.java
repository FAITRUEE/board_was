package com.example.board.controller;

import com.example.board.dto.request.CreatePostRequest;
import com.example.board.dto.response.PostListResponse;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.request.UpdatePostRequest;
import com.example.board.entity.Post;
import com.example.board.entity.PostAttachment;
import com.example.board.repository.PostAttachmentRepository;
import com.example.board.service.FileStorageService;
import com.example.board.service.PostLikeService;
import com.example.board.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final FileStorageService fileStorageService;
    private final PostAttachmentRepository attachmentRepository;

    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,  // ✅ defaultValue 제거, required = false로 변경
            @RequestParam(required = false) Long categoryId,  // ✅ 추가
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        PostListResponse response = postService.getPosts(page, size, sort, userId, categoryId);  // ✅ 순서 수정
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        PostResponse response = postService.getPost(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "isSecret", required = false, defaultValue = "false") Boolean isSecret,
            @RequestParam(value = "secretPassword", required = false) String secretPassword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setIsSecret(isSecret);
        request.setSecretPassword(secretPassword);
        request.setCategoryId(categoryId);

        PostResponse response = postService.createPost(userId, request, files);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = postService.updatePost(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            Authentication authentication) {

        System.out.println("=== 게시글 삭제 요청 ===");
        System.out.println("Post ID: " + id);
        System.out.println("Authentication: " + authentication);
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Principal Type: " + authentication.getPrincipal().getClass().getName());

        try {
            Long userId = (Long) authentication.getPrincipal();
            System.out.println("User ID: " + userId);
            postService.deletePost(userId, id);
            return ResponseEntity.noContent().build();
        } catch (ClassCastException e) {
            System.err.println("Principal을 Long으로 캐스팅 실패!");
            e.printStackTrace();
            throw new IllegalArgumentException("인증 정보가 올바르지 않습니다.");
        }
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        postService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Map<String, Object> result = postLikeService.toggleLike(userId, id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attachments/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        PostAttachment attachment = attachmentRepository.findAll().stream()
                .filter(a -> a.getStoredFileName().equals(fileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .body(resource);
    }

    // ✅ 비밀글 비밀번호 확인 엔드포인트
    @PostMapping("/{id}/verify-password")
    public ResponseEntity<PostResponse> verifySecretPost(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        String password = request.get("password");

        PostResponse response = postService.getSecretPost(id, password, userId);
        return ResponseEntity.ok(response);
    }
}