package com.example.board.service;

import com.example.board.entity.Post;
import com.example.board.entity.PostLike;
import com.example.board.entity.User;
import com.example.board.repository.PostLikeRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        boolean isLiked;

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            // 이미 좋아요한 경우 - 취소
            postLikeRepository.deleteByPostIdAndUserId(postId, userId);
            post.decrementLikes();
            isLiked = false;
        } else {
            // 좋아요 추가
            PostLike postLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(postLike);
            post.incrementLikes();
            isLiked = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", post.getLikeCount());

        return result;
    }

    public boolean isLikedByUser(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }
}