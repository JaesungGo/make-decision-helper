package com.example.make_decision_helper.service.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.repository.chatroom.ChatRoomRepository;
import com.example.make_decision_helper.repository.chatroom.InviteCodeRepository;
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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestUserServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private InviteCodeRepository inviteCodeRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    GuestUserService guestUserService;

    private ChatRoom chatRoom;
    private InviteCode inviteCode;
    private final String nickname = "testUser";
    private final Long roomId = 1L;
    private final String inviteCodeString = "testCode";

    @BeforeEach
    void beforeEach() {
        chatRoom = ChatRoom.builder()
                .id(roomId)
                .participants(new ArrayList<>())
                .build();

        inviteCode = InviteCode.builder()
                .inviteCode(inviteCodeString)
                .build();
    }

    @Test
    @DisplayName("게스트 토큰 생성 및 저장(Redis) 테스트")
    void guestTokenSuccess() {
        // given
        String expectedToken = "test.jwt.token";
        String guestKey = String.format("roomId:%d:nickname:%s", roomId, nickname);
        String guestId = String.format("GUEST_%d_%s", roomId, nickname);

        when(inviteCodeRepository.findByInviteCode(inviteCodeString)).thenReturn(Optional.of(inviteCode));
        when(chatRoomRepository.findChatRoomByInviteCode(inviteCode)).thenReturn(Optional.of(chatRoom));
        when(redisTemplate.hasKey(guestKey)).thenReturn(false);
        when(jwtTokenProvider.createGuestToken(guestId, roomId, nickname)).thenReturn(expectedToken);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        String resultToken = guestUserService.createGuestToken(inviteCodeString, nickname);

        // then
        assertEquals(expectedToken, resultToken);
        verify(valueOperations).set(eq(guestKey), eq(expectedToken), eq(24L), eq(TimeUnit.HOURS));
        verify(jwtTokenProvider).createGuestToken(eq(guestId), eq(roomId), eq(nickname));
    }

    @Test
    @DisplayName("채팅방에 이미 닉네임이 존재하는 경우 예외 테스트(Redis)")
    void guestTokenExistInRedis() {
        // given
        String guestKey = String.format("roomId:%d:nickname:%s", roomId, nickname);
        
        when(inviteCodeRepository.findByInviteCode(inviteCodeString)).thenReturn(Optional.of(inviteCode));
        when(chatRoomRepository.findChatRoomByInviteCode(inviteCode)).thenReturn(Optional.of(chatRoom));
        when(redisTemplate.hasKey(guestKey)).thenReturn(true);

        // when then
        assertThrows(InvalidRequestStateException.class,
                () -> guestUserService.createGuestToken(inviteCodeString, nickname));

        verify(jwtTokenProvider, never()).createGuestToken(anyString(), anyLong(), anyString());
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("채팅방에 이미 닉네임이 존재하는 경우 예외 테스트(MySQL)")
    void guestTokenExistInDB() {
        // given
        ChatUser chatUser = ChatUser.builder()
                .nickname(nickname)
                .build();
        chatRoom.getParticipants().add(chatUser);

        String guestKey = String.format("roomId:%d:nickname:%s", roomId, nickname);
        
        when(inviteCodeRepository.findByInviteCode(inviteCodeString)).thenReturn(Optional.of(inviteCode));
        when(chatRoomRepository.findChatRoomByInviteCode(inviteCode)).thenReturn(Optional.of(chatRoom));
        when(redisTemplate.hasKey(guestKey)).thenReturn(false);

        // when then
        assertThrows(InvalidRequestStateException.class,
                () -> guestUserService.createGuestToken(inviteCodeString, nickname));

        verify(jwtTokenProvider, never()).createGuestToken(anyString(), anyLong(), anyString());
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("존재하지 않는 초대 코드로 요청시 예외 테스트")
    void invalidInviteCode() {
        // given
        when(inviteCodeRepository.findByInviteCode(inviteCodeString))
                .thenReturn(Optional.empty());

        // when then
        assertThrows(NoSuchElementException.class,
                () -> guestUserService.createGuestToken(inviteCodeString, nickname));
    }
}