package com.example.board.dto.team;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberInviteRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    private String role;  // ADMIN, MEMBER (기본값: MEMBER)
}