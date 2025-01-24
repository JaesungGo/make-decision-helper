package com.example.make_decision_helper.service.chat;

import com.example.make_decision_helper.domain.chat.ChatMessage;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageRequest;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageResponse;
import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.repository.chat.ChatMessageRepository;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.repository.chatuser.ChatUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatMessageResponse saveAndGetMessage(ChatMessageRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nickname = auth.getName();
        
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));
        
        ChatUser sender = chatUserRepository.findByChatRoomAndNickname(chatRoom, nickname)
                .orElseThrow(() -> new NoSuchElementException("채팅 참여자를 찾을 수 없습니다."));

        ChatMessage message = ChatMessage.builder()
                .roomId(chatRoom.getId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);
        return ChatMessageResponse.from(savedMessage);
    }

    public List<ChatMessageResponse> getChatMessages(Long roomId, int size) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(
            roomId, 
            PageRequest.of(0, size)
        );
        
        return messages.stream()
                .map(ChatMessageResponse::from)
                .toList();
    }
    
    public void deleteRoomMessages(Long roomId) {
        chatMessageRepository.deleteByRoomId(roomId);
    }
}
