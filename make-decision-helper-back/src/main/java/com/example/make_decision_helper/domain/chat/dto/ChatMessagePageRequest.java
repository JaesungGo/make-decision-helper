package com.example.make_decision_helper.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessagePageRequest {
    private int page;
    private int size;
    private Long lastMessageId;
    
    public static ChatMessagePageRequest of(int page, int size, Long lastMessageId) {
        return ChatMessagePageRequest.builder()
                .page(page)
                .size(size)
                .lastMessageId(lastMessageId)
                .build();
    }
} 