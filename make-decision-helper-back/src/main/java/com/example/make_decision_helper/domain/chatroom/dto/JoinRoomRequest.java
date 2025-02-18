package com.example.make_decision_helper.domain.chatroom.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinRoomRequest {

    @NotNull
    private String inviteCode;

    @NotNull
    @Size(min = 2, max = 20)
    private String nickname;

}
