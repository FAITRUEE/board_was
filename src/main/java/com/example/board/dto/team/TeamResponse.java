package com.example.board.dto.team;

import com.example.board.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamResponse {

    private Long id;
    private String name;
    private String description;
    private Long createdById;
    private String createdByUsername;
    private Integer memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamResponse from(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdById(team.getCreatedBy().getId())
                .createdByUsername(team.getCreatedBy().getUsername())
                .memberCount(team.getMembers().size())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}