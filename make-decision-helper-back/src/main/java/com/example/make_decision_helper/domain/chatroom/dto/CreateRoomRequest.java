package com.example.make_decision_helper.domain.chatroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "방 제목은 필수입니다")
    private String roomName;

    @NotNull(message = "최대 참여 인원은 필수입니다")
    private int maxParticipants;

    @NotNull(message = "방 유지 시간은 필수입니다")
    private int duration;

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

}
