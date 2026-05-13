package com.example.board.dto.collab;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CollabRoomCreateRequest {
    private Long teamId;
    private String title;
}
