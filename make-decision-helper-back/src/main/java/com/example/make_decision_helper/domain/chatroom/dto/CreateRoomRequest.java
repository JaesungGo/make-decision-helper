package com.example.make_decision_helper.domain.chatroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Duration;

@Data
public class CreateRoomRequest {

    @NotNull
    private String roomName;

    @NotNull
    private Integer maxParticipants;

    @NotNull
    private Integer duration;

}
