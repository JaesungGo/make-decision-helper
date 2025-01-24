package com.example.make_decision_helper.domain.chat;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Getter
public class ChatMessage {
    
    @Id
    private String id;
    
    @Indexed
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private LocalDateTime sentAt;
    
    @Builder
    public ChatMessage(Long roomId, Long senderId, String senderNickname, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }
} 