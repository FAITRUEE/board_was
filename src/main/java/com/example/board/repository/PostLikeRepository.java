package com.example.board.repository;

import com.example.board.entity.Post;
import com.example.board.entity.PostLike;
import com.example.board.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // Post와 User 엔티티로 조회
    Optional<PostLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    void deleteByPostAndUser(Post post, User user);
    long countByPost(Post post);

    // ID로 조회 (PostLikeService에서 사용)
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
}