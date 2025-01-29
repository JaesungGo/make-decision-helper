package com.example.make_decision_helper.controller.socket;

import com.example.make_decision_helper.domain.chat.dto.ChatMessageRequest;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageResponse;
import com.example.make_decision_helper.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate template;

    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest message) {
        ChatMessageResponse response = chatService.saveAndGetMessage(message);
        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), response);
    }

//    @MessageMapping("/chat/enter")
//    public void enter(ChatMessageRequest message) {
//        ChatMessageResponse response = chatService.saveAndGetMessage(message);
//        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), response);
//    }
//
//    @MessageMapping("/chat/leave")
//    public void leave(ChatMessageRequest message) {
//        ChatMessageResponse response = chatService.saveAndGetMessage(message);
//        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), response);
//    }
}