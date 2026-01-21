package com.example.board.service;

import com.example.board.dto.request.CreatePostRequest;
import com.example.board.dto.response.PostListResponse;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.request.UpdatePostRequest;
import com.example.board.entity.*;
import com.example.board.repository.*;
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
    private final CategoryRepository categoryRepository;
    private final PostLikeService postLikeService;
    private final PostLikeRepository postLikeRepository;  // ✅ 추가
    private final FileStorageService fileStorageService;
    private final PostAttachmentRepository attachmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagService tagService;

    public PostListResponse getPosts(int page, int size, String sort, Long userId, Long categoryId, String tagName, String keyword) {
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
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // ✅ 검색어가 있는 경우
        Page<Post> postPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색 + 필터 조합
            if (categoryId != null && tagName != null && !tagName.isEmpty()) {
                // 검색 + 카테고리 + 태그
                postPage = postRepository.searchByKeywordAndCategoryIdAndTagName(keyword, categoryId, tagName, pageable);
            } else if (categoryId != null) {
                // 검색 + 카테고리
                postPage = postRepository.searchByKeywordAndCategoryId(keyword, categoryId, pageable);
            } else if (tagName != null && !tagName.isEmpty()) {
                // 검색 + 태그
                postPage = postRepository.searchByKeywordAndTagName(keyword, tagName, pageable);
            } else {
                // 검색만
                postPage = postRepository.searchByKeyword(keyword, pageable);
            }
        } else {
            // 기존 필터링 로직 (검색어 없음)
            if (categoryId != null && tagName != null && !tagName.isEmpty()) {
                postPage = postRepository.findByCategoryIdAndTagName(categoryId, tagName, pageable);
            } else if (categoryId != null) {
                postPage = postRepository.findByCategoryId(categoryId, pageable);
            } else if (tagName != null && !tagName.isEmpty()) {
                postPage = postRepository.findByTagName(tagName, pageable);
            } else {
                postPage = postRepository.findAll(pageable);
            }
        }

        List<PostResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
                    if (post.getIsSecret()) {
                        if (userId != null && userId.equals(post.getAuthor().getId())) {
                            boolean isLiked = postLikeService.isLikedByUser(post.getId(), userId);
                            return PostResponse.fromEntity(post, isLiked);
                        }
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

    @Transactional(readOnly = true)
    public PostResponse getSecretPost(Long id, String password, Long userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getIsSecret()) {
            throw new IllegalArgumentException("비밀글이 아닙니다.");
        }

        if (userId != null && userId.equals(post.getAuthor().getId())) {
            boolean isLiked = postLikeService.isLikedByUser(id, userId);
            return PostResponse.fromEntity(post, isLiked);
        }

        if (password == null || !passwordEncoder.matches(password, post.getSecretPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        boolean isLiked = postLikeService.isLikedByUser(id, userId);
        return PostResponse.fromEntity(post, isLiked);
    }

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request, List<MultipartFile> files) {
        try {
            System.out.println("=== PostService.createPost 시작 ===");
            System.out.println("UserId: " + userId);
            System.out.println("Request: " + request);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.out.println(">>> User not found: " + userId);
                        return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                    });

            System.out.println(">>> User found: " + user.getUsername());

            Category category = null;
            if (request.getCategoryId() != null) {
                category = categoryRepository.findById(request.getCategoryId())
                        .orElse(null);
                System.out.println(">>> Category: " + (category != null ? category.getName() : "null"));
            }

            String encodedPassword = null;
            if (request.getIsSecret() != null && request.getIsSecret() && request.getSecretPassword() != null) {
                encodedPassword = passwordEncoder.encode(request.getSecretPassword());
                System.out.println(">>> 비밀번호 암호화 완료");
            }

            Post post = Post.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .author(user)
                    .category(category)
                    .views(0)
                    .likeCount(0)
                    .commentCount(0)
                    .isSecret(request.getIsSecret() != null ? request.getIsSecret() : false)
                    .secretPassword(encodedPassword)
                    .build();

            System.out.println(">>> Post 빌더 완료");

            if (request.getTags() != null && !request.getTags().isEmpty()) {
                System.out.println(">>> 태그 처리 시작: " + request.getTags());
                try {
                    List<Tag> tags = tagService.getOrCreateTags(request.getTags());
                    System.out.println(">>> 태그 조회/생성 완료: " + tags.size() + "개");

                    for (Tag tag : tags) {
                        post.addTag(tag);
                    }
                    System.out.println(">>> 태그 추가 완료");
                } catch (Exception e) {
                    System.err.println("!!! 태그 처리 실패 !!!");
                    e.printStackTrace();
                    throw e;
                }
            }

            System.out.println(">>> Post 저장 시작");
            Post savedPost = postRepository.save(post);
            System.out.println(">>> Post 저장 완료: " + savedPost.getId());

            if (files != null && !files.isEmpty()) {
                System.out.println(">>> 파일 업로드 시작: " + files.size() + "개");
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
                        System.err.println("!!! 파일 업로드 실패: " + file.getOriginalFilename());
                        e.printStackTrace();
                        throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                    }
                }
                System.out.println(">>> 파일 업로드 완료");
            }

            System.out.println("=== PostService.createPost 완료 ===");
            return PostResponse.fromEntity(savedPost, false);

        } catch (Exception e) {
            System.err.println("!!! PostService.createPost 실패 !!!");
            e.printStackTrace();
            throw e;
        }
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

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElse(null);
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        if (request.getTags() != null) {
            post.clearTags();

            if (!request.getTags().isEmpty()) {
                List<Tag> tags = tagService.getOrCreateTags(request.getTags());
                for (Tag tag : tags) {
                    post.addTag(tag);
                }
            }
        }

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

    // ✅ 좋아요 토글
    @Transactional
    public PostResponse toggleLike(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 좋아요 존재 여부 확인
        boolean isLiked = postLikeRepository.existsByPostAndUser(post, user);

        if (isLiked) {
            // 좋아요 취소
            postLikeRepository.deleteByPostAndUser(post, user);
            post.decrementLikeCount();  // 좋아요 수 감소
        } else {
            // 좋아요 추가
            PostLike postLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(postLike);
            post.incrementLikeCount();  // 좋아요 수 증가
        }

        // 최신 좋아요 수
        long likeCount = postLikeRepository.countByPost(post);

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .isLiked(!isLiked)  // 토글 후 상태
                .likeCount((int) likeCount)
                .build();
    }
}