package com.example.board.service;

import com.example.board.dto.request.CreatePostRequest;
import com.example.board.dto.response.PostListResponse;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.request.UpdatePostRequest;
import com.example.board.entity.Post;
import com.example.board.entity.PostAttachment;
import com.example.board.entity.User;
import com.example.board.repository.PostAttachmentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeService postLikeService;
    private final FileStorageService fileStorageService;  // ✅ 추가
    private final PostAttachmentRepository attachmentRepository;  // ✅ 추가
    private final PasswordEncoder passwordEncoder;  // ✅ 추가

    public PostListResponse getPosts(int page, int size, String sort, Long userId) {
        Pageable pageable;

        // sort 파라미터 파싱
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            String property = sortParams[0];
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            pageable = PageRequest.of(page, size, Sort.by(direction, property));
        } else {
            // 기본값: 최신순
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Post> postPage = postRepository.findAll(pageable);

        List<PostResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
                    // ✅ 비밀글 처리
                    if (post.getIsSecret()) {
                        // 작성자 본인이면 전체 내용 표시
                        if (userId != null && userId.equals(post.getAuthor().getId())) {
                            boolean isLiked = postLikeService.isLikedByUser(post.getId(), userId);
                            return PostResponse.fromEntity(post, isLiked);
                        }
                        // 작성자가 아니면 요약만 표시
                        return PostResponse.secretPostSummary(post);
                    }

                    boolean isLiked = postLikeService.isLikedByUser(post.getId(), userId);
                    return PostResponse.fromEntity(post, isLiked);
                })
                .collect(Collectors.toList());

        return PostListResponse.builder()
                .posts(postResponses)
                .total((int) postPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(postPage.getTotalPages())
                .build();
    }

    // 게시글 상세 조회
    public PostResponse getPost(Long id, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        System.out.println("=== 게시글 조회 디버깅 ===");
        System.out.println("Post ID: " + id);
        System.out.println("Is Secret: " + post.getIsSecret());
        System.out.println("Author ID: " + post.getAuthor().getId());
        System.out.println("Current User ID: " + userId);

        if (post.getIsSecret()) {
            if (userId == null || !userId.equals(post.getAuthor().getId())) {
                System.out.println(">>> 비밀글 요약 반환");
                return PostResponse.secretPostSummary(post);
            }
            System.out.println(">>> 작성자 본인 - 전체 내용 반환");
        }

        boolean isLiked = postLikeService.isLikedByUser(id, userId);
        return PostResponse.fromEntity(post, isLiked);
    }

    // ✅ 비밀번호로 비밀글 조회
    @Transactional(readOnly = true)
    public PostResponse getSecretPost(Long id, String password, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getIsSecret()) {
            throw new IllegalArgumentException("비밀글이 아닙니다.");
        }

        // 작성자 본인이면 바로 허용
        if (userId != null && userId.equals(post.getAuthor().getId())) {
            boolean isLiked = postLikeService.isLikedByUser(id, userId);
            return PostResponse.fromEntity(post, isLiked);
        }

        // 비밀번호 확인
        if (password == null || !passwordEncoder.matches(password, post.getSecretPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        boolean isLiked = postLikeService.isLikedByUser(id, userId);
        return PostResponse.fromEntity(post, isLiked);
    }

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request, List<MultipartFile> files) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // ✅ 비밀번호 암호화
        String encodedPassword = null;
        if (request.getIsSecret() != null && request.getIsSecret() && request.getSecretPassword() != null) {
            encodedPassword = passwordEncoder.encode(request.getSecretPassword());
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user)
                .views(0)
                .likeCount(0)
                .commentCount(0)
                .isSecret(request.getIsSecret() != null ? request.getIsSecret() : false)  // ✅ 추가
                .secretPassword(encodedPassword)  // ✅ 추가
                .build();

        Post savedPost = postRepository.save(post);

        // 파일 업로드 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String storedFileName = fileStorageService.storeFile(file);
                    String filePath = "/api/posts/attachments/" + storedFileName;

                    PostAttachment attachment = PostAttachment.builder()
                            .post(savedPost)
                            .originalFileName(file.getOriginalFilename())
                            .storedFileName(storedFileName)
                            .filePath(filePath)
                            .fileSize(file.getSize())
                            .contentType(file.getContentType())
                            .build();

                    attachmentRepository.save(attachment);
                } catch (Exception e) {
                    throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }

        return PostResponse.fromEntity(savedPost, false);
    }

    @Transactional
    public PostResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        boolean isLiked = postLikeService.isLikedByUser(postId, userId);

        return PostResponse.fromEntity(post, isLiked);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    @Transactional
    public void incrementViews(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.incrementViews();
    }
}