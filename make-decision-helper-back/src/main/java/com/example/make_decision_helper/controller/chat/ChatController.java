package com.example.make_decision_helper.controller.chat;

import com.example.make_decision_helper.domain.chat.dto.ChatMessageRequest;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageResponse;
import com.example.make_decision_helper.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageRequest request) {
        ChatMessageResponse response = chatService.saveAndGetMessage(request);
        messagingTemplate.convertAndSend("/sub/chat/room/" + request.getRoomId(), response);
    }
} 