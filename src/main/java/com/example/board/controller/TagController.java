package com.example.board.controller;

import com.example.board.dto.response.TagResponse;
import com.example.board.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // 모든 태그 조회
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    // 인기 태그 조회 (상위 20개)
    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getPopularTags() {
        return ResponseEntity.ok(tagService.getPopularTags());
    }

    // 태그 검색
    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> searchTags(@RequestParam String keyword) {
        return ResponseEntity.ok(tagService.searchTags(keyword));
    }

    // 사용되지 않는 태그 삭제 (관리자용)
    @DeleteMapping("/unused")
    public ResponseEntity<Void> deleteUnusedTags() {
        tagService.deleteUnusedTags();
        return ResponseEntity.noContent().build();
    }
}