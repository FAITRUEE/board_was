package com.example.board.service;

import com.example.board.dto.request.CreatePostRequest;
import com.example.board.dto.response.PostListResponse;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.request.UpdatePostRequest;
import com.example.board.entity.Post;
import com.example.board.entity.User;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeService postLikeService;

    public PostListResponse getPosts(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findAll(pageable);

        List<PostResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
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

        boolean isLiked = postLikeService.isLikedByUser(id, userId);

        return PostResponse.fromEntity(post, isLiked);
    }

    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(user)
                .views(0)
                .likeCount(0)
                .commentCount(0)
                .build();

        Post savedPost = postRepository.save(post);

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