package com.example.board.dto.response;

import com.example.board.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String name;
    private Integer useCount;

    public static TagResponse fromEntity(Tag tag) {
        if (tag == null) return null;

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .useCount(tag.getUseCount())
                .build();
    }
}