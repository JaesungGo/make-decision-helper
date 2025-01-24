package com.example.make_decision_helper.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    
    @NotNull
    private Long roomId;
    
    @NotBlank
    private String content;
} 