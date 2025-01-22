package com.example.make_decision_helper.config.security;

import com.example.make_decision_helper.domain.user.UserRole;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 들어오는 STOMP 메시지에 대한 처리
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            // 메시지가 실행되기 전의 인터셉터
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> headers = (Map<String, List<String>>)
                            message.getHeaders().get(StompHeaderAccessor.NATIVE_HEADERS);

                    if (headers != null && headers.containsKey("cookie")) {
                        String accessToken = CookieUtil.extractTokenFromWebSocketCookie(headers, "accessToken");
                        String guestToken = CookieUtil.extractTokenFromWebSocketCookie(headers, "guestToken");
                        handleAuthentication(accessor, accessToken, guestToken);
                    }
                }
                return message;
            }
        });
    }

    /**
     * 일반 사용자일 때, 게스트 사용자일 때 인증 처리
     * @param accessor
     * @param accessToken
     * @param guestToken
     */
    private void handleAuthentication(StompHeaderAccessor accessor, String accessToken, String guestToken) {

        // 일반 사용자 (accessToken)
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
            accessor.setUser(auth);
        }

        // 게스트 사용자 (guestToken)
        else if (guestToken != null && jwtTokenProvider.validateToken(guestToken)) {
            Claims claims = jwtTokenProvider.getClaims(guestToken);
            if (UserRole.GUEST.name().equals(claims.get("role", String.class))) {
                Long roomId = claims.get("roomId", Long.class);
                String nickname = claims.get("nickname", String.class);
                String nicknameKey = String.format("room:%d:nickname:%s", roomId, nickname);
                String savedToken = redisTemplate.opsForValue().get(nicknameKey);

                if (guestToken.equals(savedToken)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(guestToken);
                    accessor.setUser(auth);
                }
            }
        }
    }
}
