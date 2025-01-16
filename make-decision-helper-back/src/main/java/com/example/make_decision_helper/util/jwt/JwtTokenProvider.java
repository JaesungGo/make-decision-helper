package com.example.make_decision_helper.util.jwt;

import com.example.make_decision_helper.domain.user.UserRole;
import com.example.make_decision_helper.domain.user.CustomUserDetails;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenVal;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenVal;

    private final RedisTemplate<String, String> redisTemplate;
    private final CookieUtil cookieUtil;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    /**
     * AccessToken, RefreshToken 발급 및 쿠키에 담아 반환
     * @param email
     * @param role
     * @return Cookie Map
     */
    public Map<String, ResponseCookie> createTokenAndCookies(String email, UserRole role){
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role.name());
        log.debug("토큰 생성 시작 - email: {}, role: {}", email, role);

        Date now = new Date();
        Date accessTokenDate = new Date(now.getTime() + accessTokenVal);
        Date refreshTokenDate = new Date(now.getTime() + refreshTokenVal);

        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(accessTokenDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(refreshTokenDate)
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();


        redisTemplate.opsForValue()
                .set("RefreshToken:" + email, refreshToken, refreshTokenVal, TimeUnit.MILLISECONDS);


        Map<String, ResponseCookie> cookies =  new HashMap<>();
        cookies.put("accessToken", cookieUtil.createCookie("accessToken",accessToken,accessTokenVal/1000));
        cookies.put("refreshToken", cookieUtil.createCookie("refreshToken",refreshToken,refreshTokenVal/1000));

        return cookies;
    }

    /**
     * AccessToken 블랙리스트 처리 및 쿠키 삭제
     * @param accessToken
     * @return 만료된 쿠키가 담긴 Map
     */
    public Map<String, ResponseCookie> deleteTokenAndCookies(String accessToken) {
        try {
            String email = getEmailFromToken(accessToken);

            // AccessToken 블랙리스트 처리
            Long accessTokenExpiration = getExpirationFromToken(accessToken);
            if (accessTokenExpiration > 0) {
                redisTemplate.opsForValue()
                        .set("BlackList:AT:" + accessToken, email, accessTokenExpiration, TimeUnit.MILLISECONDS);
                log.info("AccessToken BlackList 추가 완료: {}", email);
            }

            return cookieUtil.createLogoutCookie();
        } catch (Exception e) {
            log.error("토큰 처리 중 에러 발생: {}", e.getMessage());
            throw new RuntimeException("토큰 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * AccessToken 만료 시 토큰 제발급
     * @param refreshToken
     * @return
     */
    public Map<String, ResponseCookie> reissueAccessToken(String refreshToken) {
        String email = getEmailFromToken(refreshToken);
        String savedRefreshToken = (String) redisTemplate.opsForValue().get("RefreshToken:" + email);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않는 AccessToken");
        }

        return createTokenAndCookies(email, UserRole.USER);
    }

    /**
     * 토큰에서 이메일 추출
     * @param token
     * @return 이메일
     */
    public String getEmailFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * 토큰 유효성 검증
     * @param token
     * @return 토큰이 유효하면 true,
     */
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return !isTokenBlacklisted(token, "AT");

        } catch(Exception e){
            return false;
        }
    }

    /**
     * Authentication 반환 받기
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token) {
        log.debug("토큰 Authentication 변환 시작");

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.getSubject();
            String roleString = claims.get("role",String.class);



            // CustomUserDetails 생성 (User 엔티티의 필요한 정보만 포함)
            CustomUserDetails userDetails = new CustomUserDetails(
                    email,
                    "",  // password는 비워둠
                    Collections.singleton(new SimpleGrantedAuthority(roleString))
            );


            return UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        } catch (Exception e) {
            log.error("Authentication 생성 중 오류 발생", e);
            throw new AuthenticationException("Authentication 생성 실패: " + e.getMessage()) {};
        }
    }

    /**
     * 토큰에서 유효시간 추출
     * @param token
     * @return
     */
    private Long getExpirationFromToken(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 토큰 블랙리스트 검증
     * @param token 검증할 토큰
     * @param tokenType 토큰 타입 ("AT" 또는 "RT")
     * @return 블랙리스트에 있으면 true
     */
    public boolean isTokenBlacklisted(String token, String tokenType) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("BlackList:" + tokenType + ":" + token));
    }

}
