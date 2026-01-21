package com.example.board.controller;

import com.example.board.dto.request.CreatePostRequest;
import com.example.board.dto.request.UpdatePostRequest;
import com.example.board.dto.response.PostListResponse;
import com.example.board.dto.response.PostResponse;
import com.example.board.entity.User;
import com.example.board.repository.UserRepository;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    // ✅ userId 추출 헬퍼 메서드
    private Long getUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        try {
            return (Long) authentication.getPrincipal();
        } catch (ClassCastException e) {
            return null;
        }
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<PostListResponse> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tagName) {

        Long userId = getUserIdFromAuthentication();
        PostListResponse response = postService.getPosts(page, size, sort, userId, categoryId, tagName);
        return ResponseEntity.ok(response);
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        Long userId = getUserIdFromAuthentication();
        PostResponse response = postService.getPost(id, userId);
        return ResponseEntity.ok(response);
    }

    // 비밀글 비밀번호 확인
    @PostMapping("/{id}/verify-password")
    public ResponseEntity<PostResponse> verifySecretPost(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String password = request.get("password");
        Long userId = getUserIdFromAuthentication();
        PostResponse response = postService.getSecretPost(id, password, userId);
        return ResponseEntity.ok(response);
    }

    // ✅ 첨부파일 다운로드/조회
    @GetMapping("/attachments/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            System.out.println("=== 파일 요청 ===");
            System.out.println("Filename: " + filename);

            // 파일 저장 경로 (application.properties의 file.upload-dir와 동일해야 함)
            Path filePath = Paths.get("uploads").resolve(filename).normalize();
            System.out.println("File Path: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = "application/octet-stream";

                // 이미지 파일인 경우 적절한 Content-Type 설정
                String lowerFilename = filename.toLowerCase();
                if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowerFilename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lowerFilename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (lowerFilename.endsWith(".webp")) {
                    contentType = "image/webp";
                } else if (lowerFilename.endsWith(".svg")) {
                    contentType = "image/svg+xml";
                }

                System.out.println(">>> 파일 찾음: " + filename + " (Type: " + contentType + ")");

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.err.println(">>> 파일 없음: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("!!! 파일 다운로드 실패 !!!");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ 게시글 작성
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "isSecret", required = false) Boolean isSecret,
            @RequestParam(value = "secretPassword", required = false) String secretPassword,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        try {
            System.out.println("=== 게시글 작성 요청 ===");
            System.out.println("Title: " + title);
            System.out.println("Content: " + content);
            System.out.println("CategoryId: " + categoryId);
            System.out.println("Tags: " + tags);
            System.out.println("IsSecret: " + isSecret);
            System.out.println("Files: " + (files != null ? files.size() : 0));

            Long userId = getUserIdFromAuthentication();
            System.out.println("User ID from authentication: " + userId);

            if (userId == null) {
                System.out.println(">>> 인증 실패: userId is null");
                return ResponseEntity.status(401).build();
            }

            System.out.println(">>> 인증 성공, 사용자 ID: " + userId);

            CreatePostRequest request = CreatePostRequest.builder()
                    .title(title)
                    .content(content)
                    .categoryId(categoryId)
                    .tags(tags)
                    .isSecret(isSecret)
                    .secretPassword(secretPassword)
                    .build();

            System.out.println(">>> Request 생성 완료");

            PostResponse response = postService.createPost(userId, request, files);

            System.out.println(">>> 게시글 작성 성공: " + response.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("!!! 게시글 작성 실패 !!!");
            e.printStackTrace();
            throw e;
        }
    }

    // ✅ 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request) {

        Long userId = getUserIdFromAuthentication();

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        PostResponse response = postService.updatePost(userId, id, request);
        return ResponseEntity.ok(response);
    }

    // ✅ 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        Long userId = getUserIdFromAuthentication();

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        postService.deletePost(userId, id);
        return ResponseEntity.noContent().build();
    }

    // 조회수 증가
    @PostMapping("/{id}/views")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        postService.incrementViews(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ 좋아요 토글
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Long userId = getUserIdFromAuthentication();

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        PostResponse response = postService.toggleLike(id, userId);

        return ResponseEntity.ok(Map.of(
                "isLiked", response.getIsLiked(),
                "likeCount", response.getLikeCount()
        ));
    }
}