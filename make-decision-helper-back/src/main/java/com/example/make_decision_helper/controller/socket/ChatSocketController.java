package com.example.make_decision_helper.controller.socket;

import com.example.make_decision_helper.domain.chat.dto.ChatMessageRequest;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageResponse;
import com.example.make_decision_helper.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;
    private static final int MAX_MESSAGE_LENGTH = 1000;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest message, Principal principal) {
        try {
            validateMessage(message);
            
            String sanitizedContent = HtmlUtils.htmlEscape(message.getContent());
            message.setContent(sanitizedContent);

            ChatMessageResponse savedMessage = chatService.saveAndGetMessage(message);
            template.convertAndSend("/sub/chat/room/" + message.getRoomId(), savedMessage);
            
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private void validateMessage(ChatMessageRequest message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다");
        }
        if (message.getContent().length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("메시지가 너무 깁니다");
        }
    }
}