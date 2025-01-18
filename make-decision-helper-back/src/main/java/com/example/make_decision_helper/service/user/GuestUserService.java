package com.example.make_decision_helper.service.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j @RequiredArgsConstructor
public class GuestUserService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 게스트 토큰 생성 및 Redis에 임시 저장
     * @param chatRoom
     * @param nickname
     * @return
     */
    public String guestToken(ChatRoom chatRoom, String nickname){

        Long roomId = chatRoom.getId();
        String guestId = String.format("GUEST_%d_s", roomId, nickname);


        String guestKey = String.format("roomId:%:nickname:%s", roomId, nickname);
        Boolean isKeyExists = redisTemplate.hasKey(guestKey);
        if(isKeyExists ||  isNicknameExist(chatRoom,nickname) ){
            throw new InvalidRequestStateException("이미 사용중인 닉네임입니다");
        }

        String guestToken = jwtTokenProvider.createGuestToken(guestId,roomId,nickname);

        redisTemplate.opsForValue().set(guestKey, guestToken, 24, TimeUnit.HOURS);

        return guestToken;
    }

    /**
     * 채팅방에 이미 닉네임이 존재하는지 확인
     * @param chatRoom
     * @param nickname
     * @return 존재하면 true 아니면 false
     */
    private boolean isNicknameExist(ChatRoom chatRoom, String nickname){
        for (ChatUser participant : chatRoom.getParticipants()) {
            if(nickname == participant.getNickname()) return true;
        }
        return false;
    }

}