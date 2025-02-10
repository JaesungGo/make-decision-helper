package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatroom.dto.JoinRoomRequest;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.chatuser.ChatUserType;
import com.example.make_decision_helper.domain.user.CustomUserDetails;
import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.repository.chatroom.InviteCodeRepository;
import com.example.make_decision_helper.repository.chatuser.ChatUserRepository;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import com.example.make_decision_helper.util.security.SecurityUtil;
import com.sun.jdi.request.InvalidRequestStateException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@Service("guestChatRoomServiceImpl")
@RequiredArgsConstructor
public class GuestChatRoomServiceImpl implements ChatRoomService {

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
    @Override
    public RoomResponse joinRoom(JoinRoomRequest joinRoomRequest, CustomUserDetails userDetails){

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

        // String guestKey = String.format("roomId:%d:nickname:%s", chatRoom.getId(), joinRoomRequest.getNickname());
        // if(redisTemplate.hasKey(guestKey) ||
        //         chatRoom.getParticipants().stream().anyMatch(user -> user.getNickname().equals(joinRoomRequest.getNickname()))
        // ){
        //     throw new InvalidRequestStateException("이미 사용중인 닉네임입니다");
        // }

        ChatUser guestUser = ChatUser.builder()
                .chatRoom(chatRoom)
                .user(null)
                .nickname(joinRoomRequest.getNickname())
                .type(ChatUserType.GUEST)
                .build();

        chatUserRepository.save(guestUser);

        chatRoom.addChatUser(guestUser);

        return RoomResponse.toGuest(chatRoom,guestUser,inviteCode);
    }

    /**
     * 방 나가기 (게스트)
     * @param roomId
     */
    @Transactional
    @Override
    public void leaveRoom(Long roomId, CustomUserDetails userDetails){

        String guestToken = SecurityUtil.getCurrentGuestToken()
                .orElseThrow(() -> new IllegalStateException("게스트 토큰이 없습니다"));

        if(guestToken == null && !jwtTokenProvider.validateToken(guestToken)){
            throw new IllegalStateException("유효하지 않은 토큰입니다");
        }
        Claims claims = jwtTokenProvider.getClaims(guestToken);
        roomId = claims.get("roomId", Long.class);
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

    /**
     * 채팅방 조회 (참여자 확인)
     * @param roomId
     * @return RoomResponse
     */
    @Transactional(readOnly = true)
    public RoomResponse findRoom(Long roomId) {

        String guestToken = SecurityUtil.getCurrentGuestToken()
                .orElseThrow(() -> new NoSuchElementException("게스트 토큰을 찾을 수 없습니다."));

        Claims claims = jwtTokenProvider.getClaims(guestToken);

        if(!roomId.equals(claims.get("roomId", Long.class))){
            throw new IllegalStateException("방에 참여할 수 없습니다 (게스트 토큰 불일치)");
        }
        String nicknameFindByGuestToken = claims.get("nickname", String.class);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));

        ChatUser chatUser = chatUserRepository.findByNickname(nicknameFindByGuestToken)
                .orElseThrow(() -> new AccessDeniedException("해당 채팅방에 참여하지 않은 사용자입니다."));

        return RoomResponse.from(chatRoom, chatUser, chatRoom.getInviteCode());
    }

}
