// src/main/java/com/example/board/dto/team/TeamMemberResponse.java
package com.example.board.dto.team;

import com.example.board.entity.TeamMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String role;  // OWNER, ADMIN, MEMBER
    private LocalDateTime joinedAt;

    public static TeamMemberResponse from(TeamMember member) {
        return TeamMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .email(member.getUser().getEmail())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}