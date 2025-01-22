package com.example.make_decision_helper.domain.chatroom.dto;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class RoomResponse {

    private String inviteCode;
    private String hostNickName;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDateTime expiration;
    private ChatRoom.RoomStatus status;
    private List<ChatUser> chatUsers;

}
