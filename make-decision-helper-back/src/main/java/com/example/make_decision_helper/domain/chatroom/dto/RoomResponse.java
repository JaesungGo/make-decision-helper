package com.example.make_decision_helper.domain.chatroom.dto;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.chatuser.ChatUserType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long roomId;
    private String title;
    private String inviteCode;
    private String hostNickName;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDateTime expiration;
    private String status;
    private List<ChatUserDto> participants;

    public RoomResponse(Long id, String title, String inviteCode,
                        int maxParticipants, int participantsSize, ChatUserType type) {
        this.roomId = id;
        this.title = title;
        this.inviteCode = inviteCode;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = participantsSize;
    }

    public static RoomResponse from(ChatRoom chatRoom, ChatUser currentUser, InviteCode inviteCode) {
        return RoomResponse.builder()
                .roomId(chatRoom.getId())
                .title(chatRoom.getTitle())
                .inviteCode(inviteCode.getInviteCode())
                .hostNickName(currentUser.getNickname())
                .maxParticipants(chatRoom.getMaxParticipants())
                .currentParticipants(chatRoom.getParticipants().size())
                .expiration(chatRoom.getExpirationTime())
                .status(chatRoom.getRoomStatus().name())
                .participants(chatRoom.getParticipants().stream()
                        .map(ChatUserDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
