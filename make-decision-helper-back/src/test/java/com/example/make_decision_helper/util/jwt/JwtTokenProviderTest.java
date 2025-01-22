package com.example.make_decision_helper.util.jwt;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.domain.user.UserRole;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("JWT 토큰 관련 테스트")
class JwtTokenProviderTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private final String TEST_EMAIL = "test@test.com";
    private final UserRole TEST_ROLE = UserRole.USER;
    private final String SECRET_KEY = "testSecretKeyWithMinimum32CharactersForHS256";
    private final Long ACCESS_TOKEN_EXPIRATION = 3600000L;
    private final Long REFRESH_TOKEN_EXPIRATION = 1209600000L;

    @BeforeEach
    void beforeEach(){
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenVal", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenVal", REFRESH_TOKEN_EXPIRATION);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jwtTokenProvider.init();
    }

    /**
     * JWT 토큰 값은 변동이 심하기 때문에 anyString()으로 파라미터 인자를 받음 
     */
    @Test
    @DisplayName("토큰 생성 관련 테스트")
    void createTokenAndCookies() {

        //given
        ResponseCookie testCookie = ResponseCookie.from("token", "value").build();
        try (MockedStatic<CookieUtil> cookieUtil = mockStatic(CookieUtil.class)) {
            cookieUtil.when(() -> CookieUtil.createCookie(anyString(), anyString(), anyLong()))
                    .thenReturn(testCookie);

            //when
            Map<String, ResponseCookie> testCookiesWithToken = jwtTokenProvider.createTokenAndCookies(TEST_EMAIL, TEST_ROLE);

            //then
            assertAll(
                    () -> assertTrue(testCookiesWithToken.containsKey("accessToken")),
                    () -> assertTrue(testCookiesWithToken.containsKey("refreshToken")),
                    () -> verify(valueOperations).set(
                            argThat(key -> key.startsWith("RefreshToken:")),
                            anyString(),
                            eq(REFRESH_TOKEN_EXPIRATION),
                            eq(TimeUnit.MILLISECONDS)
                    )
            );
        }
    }

//    @Test
//    @DisplayName("토큰 삭제 테스트")
//    void deleteTokenAndCookiesTest() {
//        // given
//        String testAccessToken = Jwts.builder()
//                .setSubject(TEST_EMAIL)
//                .claim("role", TEST_ROLE)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
//                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()))
//                .compact();
//
//        ResponseCookie logoutCookie = ResponseCookie.from("accessToken", "")
//                .maxAge(0)
//                .build();
//
//        when(cookieUtil.createLogoutCookie()).thenReturn(Map.of("accessToken", logoutCookie));
//
//        // when
//        Map<String, ResponseCookie> result = jwtTokenProvider.deleteTokenAndCookies(testAccessToken);
//
//        // then
//        assertThat(result).isNotNull();
//        verify(redisTemplate).delete(anyString());
//        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
//    }

    @Test
    void getEmailFromToken() {

        //given
        String testToken = createTestToken(TEST_EMAIL, TEST_ROLE);

        //when
        String emailFromToken = jwtTokenProvider.getEmailFromToken(testToken);

        //then
        assertEquals(TEST_EMAIL,emailFromToken);

    }

    @Test
    void validateToken() {

        //given
        String testToken = createTestToken(TEST_EMAIL, TEST_ROLE);

        //when, then
        assertTrue(jwtTokenProvider.validateToken(testToken));

    }

    @Test
    @DisplayName("Guest용 토큰 발급 테스트")
    void createGuestTokenText(){
        //given
        Long roomId = 1L;
        String nickname = "testUser";
        String guestId = String.format("GUEST_%d_%s", 1L, "testUser");


        //when
        String guestToken = jwtTokenProvider.createGuestToken(guestId, roomId,nickname);

        //then
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(guestToken)
                .getBody();

        assertAll(
                ()-> assertEquals(guestId, claims.getSubject()),
                ()-> assertEquals(roomId, claims.get("roomId", Long.class)),
                ()-> assertEquals(nickname, claims.get("nickname",String.class))
        );
    }

    private String createTestToken(String email, UserRole role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(SECRET_KEY.getBytes()))
                .compact();
    }

}