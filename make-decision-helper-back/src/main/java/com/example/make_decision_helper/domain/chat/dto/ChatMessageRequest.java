package com.example.make_decision_helper.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChatMessageRequest {

    @NotBlank
    private Long roomId;

    @NotBlank
    private String content;

    private LocalDateTime timestamp = LocalDateTime.now();
} 