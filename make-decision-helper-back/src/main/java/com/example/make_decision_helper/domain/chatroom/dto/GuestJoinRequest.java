package com.example.make_decision_helper.domain.chatroom.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 게스트 참여용 Request DTO
 */
@Data
public class GuestJoinRequest {

    @NotNull
    private String inviteCode;

    @NotNull
    @Size(min = 2, max = 20)
    private String nickname;

}
