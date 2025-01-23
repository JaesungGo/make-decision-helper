package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.chatuser.ChatUserType;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.repository.chatroom.InviteCodeRepository;
import com.example.make_decision_helper.repository.chatuser.ChatUserRepository;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import com.sun.jdi.request.InvalidRequestStateException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestChatRoomService {

    private final InviteCodeRepository inviteCodeRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 방 참여 (게스트)
     * @param joinRoomRequest
     * @return RoomResponse
     */
    @Transactional
    public RoomResponse joinRoom(JoinRoomRequest joinRoomRequest){

        InviteCode inviteCode = inviteCodeRepository.findByInviteCode(joinRoomRequest.getInviteCode())
                .orElseThrow(()-> new NoSuchElementException("초대 코드를 찾을 수 없습니다"));

        ChatRoom chatRoom = chatRoomRepository.findChatRoomByInviteCode(inviteCode)
                .orElseThrow(()-> new NoSuchElementException("채팅방을 찾을 수 없습니다"));

        if(!chatRoom.getRoomStatus().equals(ChatRoom.RoomStatus.ACTIVE) ||
                chatRoom.getParticipants().size() >= chatRoom.getMaxParticipants()){
            throw new InvalidRequestStateException("입장할 수 없는 상태입니다");
        }

        if (LocalDateTime.now().isAfter(chatRoom.getExpirationTime())) {
            chatRoom.changeRoomStatus(ChatRoom.RoomStatus.EXPIRED);
            chatRoomRepository.save(chatRoom);
        }

        String guestKey = String.format("roomId:%d:nickname:%s", chatRoom.getId(), joinRoomRequest.getNickname());
        if(redisTemplate.hasKey(guestKey) ||
                chatRoom.getParticipants().stream().anyMatch(user -> user.getNickname().equals(joinRoomRequest.getNickname()))
        ){
            throw new InvalidRequestStateException("이미 사용중인 닉네임입니다");
        }

        ChatUser guestUser = ChatUser.builder()
                .chatRoom(chatRoom)
                .nickname(joinRoomRequest.getNickname())
                .type(ChatUserType.GUEST)
                .build();

        chatUserRepository.save(guestUser);

        return RoomResponse.from(chatRoom,guestUser,inviteCode);

    }

    /**
     * 방 나가기 (게스트)
     * @param guestToken
     */
    @Transactional
    public void leaveRoom(String guestToken){

        if(guestToken == null && !jwtTokenProvider.validateToken(guestToken)){
            throw new IllegalStateException("유효하지 않은 토큰입니다");
        }
        Claims claims = jwtTokenProvider.getClaims(guestToken);
        Long roomId = claims.get("roomId", Long.class);
        String nickname = claims.get("nickname", String.class);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(()-> new NoSuchElementException("채팅방을 찾을 수 없습니다"));

        ChatUser guestUser = chatUserRepository.findByChatRoomAndNickname(chatRoom, nickname)
                .orElseThrow(() -> new NoSuchElementException("채팅 참여자를 찾을 수 없습니다"));

        String guestKey = String.format("roomId:%d:nickname:%s", chatRoom.getId(), guestUser.getNickname());

        redisTemplate.delete(guestKey);

        chatUserRepository.delete(guestUser);

        if(chatRoom.getParticipants().isEmpty()){
            chatRoom.changeRoomStatus(ChatRoom.RoomStatus.CLOSED);
            chatRoomRepository.save(chatRoom);
        }
    }
}
