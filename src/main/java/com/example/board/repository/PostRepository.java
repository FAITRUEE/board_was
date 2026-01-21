package com.example.board.repository;

import com.example.board.entity.Category;
import com.example.board.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.attachments WHERE p.id = :id")
    Optional<Post> findByIdWithAttachments(@Param("id") Long id);

    // 카테고리별 조회 추가
    Page<Post> findByCategory(Category category, Pageable pageable);

    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
}
