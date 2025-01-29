package com.example.make_decision_helper.controller.chat;

import com.example.make_decision_helper.domain.chat.dto.ChatMessageRequest;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageResponse;
import com.example.make_decision_helper.domain.chat.dto.ChatMessagePageRequest;
import com.example.make_decision_helper.service.chat.ChatService;
import com.example.make_decision_helper.util.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Validated
public class ChatController {

    private final ChatService chatService;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_RECENT_MESSAGES = 100;

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            @RequestParam(required = false) Long lastMessageId) {
        try {
            ChatMessagePageRequest pageRequest = ChatMessagePageRequest.of(page, size, lastMessageId);
            Page<ChatMessageResponse> messages = chatService.getChatMessages(roomId, pageRequest);
            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}/messages/recent")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getRecentMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(MAX_RECENT_MESSAGES) int limit) {
        try {
            List<ChatMessageResponse> messages = chatService.getRecentMessages(roomId, limit);
            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}