package com.example.make_decision_helper.domain.chatroom.dto;

import com.example.make_decision_helper.domain.chatuser.ChatUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserDto {
    private Long userId;
    private String nickname;
    private String userType;
    private LocalDateTime joinedAt;

    public static ChatUserDto from(ChatUser chatUser) {
        return ChatUserDto.builder()
                .userId(chatUser.getUser().getId())
                .nickname(chatUser.getNickname())
                .userType(chatUser.getType().name())
                .joinedAt(chatUser.getJoinedAt())
                .build();
    }
} 