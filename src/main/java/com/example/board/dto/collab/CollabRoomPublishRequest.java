package com.example.board.dto.collab;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CollabRoomPublishRequest {
    private Long categoryId;
    private List<String> tags;
}
