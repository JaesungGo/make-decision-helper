package com.example.make_decision_helper.service.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import com.sun.jdi.request.InvalidRequestStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GuestUserServiceTest {

    @Mock(lenient = true)
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Resis 값 조작 관련 인터페이스
     */
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    GuestUserService guestUserService;

    private ChatRoom chatRoom;
    private final String nickname = "testUser";
    private final Long roomId = 1L;

    @BeforeEach
    void beforeEach(){
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        chatRoom = ChatRoom.builder()
                .id(roomId)
                .participants(new ArrayList<>())
                .build();

    }

    @Test
    @DisplayName("게스트 토큰 생성 및 저장(Redis) 테스트")
    void guestTokenSuccess() {
        // given
        String expectedToken = "test.jwt.token";
        String guestKey = String.format("roomId:%d:nickname:%s", roomId, nickname);

        when(redisTemplate.hasKey(guestKey)).thenReturn(false);
        when(jwtTokenProvider.createGuestToken(anyString(), eq(roomId), eq(nickname)))
                .thenReturn(expectedToken);

        // when
        String resultToken = guestUserService.guestToken(chatRoom, nickname);

        // then
        assertEquals(expectedToken, resultToken);
        verify(valueOperations).set(eq(guestKey), eq(expectedToken), eq(24L), eq(TimeUnit.HOURS));
        verify(jwtTokenProvider).createGuestToken(anyString(), eq(roomId), eq(nickname));
    }

    @Test
    @DisplayName("채팅방에 이미 닉네임이 존재하는 경우 예외 테스트(Redis)")
    void guestTokenExistInRedis() {
        // given
        String guestKey = String.format("roomId:%d:nickname:%s", roomId, nickname);
        when(redisTemplate.hasKey(guestKey)).thenReturn(true);

        // when then
        assertThrows(InvalidRequestStateException.class,
                () -> guestUserService.guestToken(chatRoom, nickname),"이미 사용중인 닉네임입니다");

        verify(jwtTokenProvider, never()).createGuestToken(anyString(),any(),anyString());
        verify(valueOperations, never()).set(anyString(),anyString(),anyLong(),any());
    }

    @Test
    @DisplayName("채팅방에 이미 닉네임이 존재하는 경우 예외 테스트(MySQL)")
    void guestTokenExistInDB(){
        // given
        ChatUser chatUser = ChatUser.builder()
                .nickname(nickname)
                .build();
        chatRoom.getParticipants().add(chatUser);

        String guestKey = String.format("roomId:%d:nickname:%s", roomId, nickname);
        when(redisTemplate.hasKey(guestKey)).thenReturn(false);

        // when
        assertThrows(InvalidRequestStateException.class,
                ()-> guestUserService.guestToken(chatRoom,nickname),"이미 사용중인 닉네임입니다");

        verify(jwtTokenProvider, never()).createGuestToken(anyString(),anyLong(),anyString());
        verify(valueOperations, never()).set(anyString(),anyString(),anyLong(),any());

    }

}