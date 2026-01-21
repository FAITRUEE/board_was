package com.example.board.service;

import com.example.board.dto.response.TagResponse;
import com.example.board.entity.Tag;
import com.example.board.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    // 모든 태그 조회
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 인기 태그 조회 (상위 20개)
    public List<TagResponse> getPopularTags() {
        return tagRepository.findTop20ByOrderByUseCountDesc().stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 태그 검색
    public List<TagResponse> searchTags(String keyword) {
        return tagRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 태그 이름으로 조회 또는 생성
    @Transactional
    public Tag getOrCreateTag(String tagName) {
        String normalizedName = tagName.trim().toLowerCase();

        return tagRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .name(normalizedName)
                            .useCount(0)
                            .build();
                    return tagRepository.save(newTag);
                });
    }

    // 여러 태그 이름으로 조회 또는 생성
    @Transactional
    public List<Tag> getOrCreateTags(List<String> tagNames) {
        return tagNames.stream()
                .map(this::getOrCreateTag)
                .collect(Collectors.toList());
    }

    // 사용되지 않는 태그 삭제 (useCount = 0)
    @Transactional
    public void deleteUnusedTags() {
        List<Tag> unusedTags = tagRepository.findByUseCountGreaterThan(-1).stream()
                .filter(tag -> tag.getUseCount() == 0)
                .collect(Collectors.toList());

        tagRepository.deleteAll(unusedTags);
    }
}