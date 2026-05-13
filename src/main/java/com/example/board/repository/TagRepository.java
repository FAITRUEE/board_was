package com.example.board.repository;

import com.example.board.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // 이름으로 태그 찾기
    Optional<Tag> findByName(String name);

    // 이름에 포함된 태그 검색
    List<Tag> findByNameContainingIgnoreCase(String keyword);

    // 사용 횟수 많은 순으로 상위 N개 조회 (인기 태그)
    List<Tag> findTop20ByOrderByUseCountDesc();

    // 사용 횟수가 0보다 큰 태그만 조회
    List<Tag> findByUseCountGreaterThan(Integer count);
}