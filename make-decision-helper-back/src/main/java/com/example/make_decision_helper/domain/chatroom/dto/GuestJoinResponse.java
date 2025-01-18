package com.example.make_decision_helper.domain.chatroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * 게스트 참여용 Response DTO
 */
@Data @Builder
public class GuestJoinResponse {

    private String nickname;
    private String roomName;

}
