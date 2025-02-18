package com.example.make_decision_helper.service.chat;

import com.example.make_decision_helper.domain.chat.ChatMessage;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageRequest;
import com.example.make_decision_helper.domain.chat.dto.ChatMessageResponse;
import com.example.make_decision_helper.domain.chat.dto.ChatMessagePageRequest;
import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.repository.chat.ChatMessageRepository;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.repository.chatuser.ChatUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;

    @Transactional
    public ChatMessageResponse saveAndGetMessage(ChatMessageRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
        }

        String nickname = auth.getName();
        if (nickname == null || nickname.isEmpty()) {
            throw new IllegalStateException("사용자 정보를 찾을 수 없습니다.");
        }
        
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

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getChatMessages(Long roomId, ChatMessagePageRequest pageRequest) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));
                
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize());
        List<ChatMessage> messages;
        
        // 이미 존재한 메시지 기록이 있는 경우
        if (pageRequest.getLastMessageId() != null) {
            messages = chatMessageRepository.findByRoomIdAndIdLessThan(
                roomId, 
                pageRequest.getLastMessageId().toString(), 
                pageable
            );
        } else {
            messages = chatMessageRepository.findByRoomId(roomId, pageable);
        }
        
        // 전체 메시지 수 조회
        long totalMessages = chatMessageRepository.countByRoomId(roomId);
        
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
                
        return new PageImpl<>(messageResponses, pageable, totalMessages);
    }
    
    public void deleteRoomMessages(Long roomId) {
        chatMessageRepository.deleteByRoomId(roomId);
    }

    // 최근 메시지 조회 (채팅방 입장 시 사용)
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentMessages(Long roomId, int limit) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomId(
            roomId, 
            PageRequest.of(0, limit)
        );
        
        return messages.stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }
}
