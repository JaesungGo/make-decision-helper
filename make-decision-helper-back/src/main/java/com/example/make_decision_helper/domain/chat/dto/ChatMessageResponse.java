package com.example.make_decision_helper.domain.chat.dto;

import com.example.make_decision_helper.domain.chat.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private String messageId;
    private String content;
    private String senderNickname;
    private LocalDateTime sentAt;
    
    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .content(message.getContent())
                .senderNickname(message.getSenderNickname())
                .sentAt(message.getSentAt())
                .build();
    }
} 