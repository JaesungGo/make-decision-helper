package com.example.make_decision_helper.controller.user;

import com.example.make_decision_helper.domain.user.dto.LoginRequest;
import com.example.make_decision_helper.domain.user.dto.SignUpRequest;
import com.example.make_decision_helper.domain.user.dto.UserResponse;
import com.example.make_decision_helper.service.user.UserService;
import com.example.make_decision_helper.util.ApiResponse;
import com.example.make_decision_helper.util.cookie.CookieUtil;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate redisTemplate;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignUpRequest request) {
        try {
            userService.createUser(request);
            return ResponseEntity.ok(ApiResponse.success(null,"회원 가입 완료"));
        } catch(Exception e){
            log.error("회원가입 실패: {}",e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request, @CookieValue(name = "guestToken", required = false) String guestToken) {

        try {

            Map<String, ResponseCookie> cookies = userService.login(request);

            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.get("accessToken").toString())
                    .header(HttpHeaders.SET_COOKIE, cookies.get("refreshToken").toString());

            if( guestToken != null){
                ResponseCookie deleteGuestCookie = CookieUtil.deleteGuestCookie();
                responseBuilder.header(HttpHeaders.SET_COOKIE, deleteGuestCookie.toString());
            }

            return responseBuilder
                    .body(ApiResponse.success(null,"로그인 성공"));
        } catch (Exception e){
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpServletRequest ,String cookie) {
        try{
            String token = CookieUtil.getCookieValue(httpServletRequest,"accessToken");

            if(token == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("로그인 상태가 아닙니다."));
            }

            Map<String, ResponseCookie> cookies = userService.logout(token);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.get("accessToken").toString())
                    .header(HttpHeaders.SET_COOKIE, cookies.get("refreshToken").toString())
                    .build();

        } catch (Exception e){
            log.error("로그아웃 실패: {}",e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }

    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissue(HttpServletRequest httpServletRequest) {
        String refreshToken = CookieUtil.getCookieValue(httpServletRequest, "refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh Token이 없습니다."));
        }

        try {
            Map<String, ResponseCookie> cookies = jwtTokenProvider.reissueAccessToken(refreshToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.get("accessToken").toString())
                    .header(HttpHeaders.SET_COOKIE, cookies.get("refreshToken").toString())
                    .body(ApiResponse.success(null, "토큰이 재발급되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("토큰 재발급에 실패했습니다."));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> authUser(@AuthenticationPrincipal UserDetails userDetails){
        try{
            log.debug("authUser Controller on ");
            log.debug("userDetails 값: {},{}",userDetails.getUsername(), userDetails.getAuthorities());
            UserResponse userResponse = userService.authUser(userDetails);
            log.debug("userResponse 값: {},{}",userResponse.getUsername(), userResponse.getUserRole());
            return ResponseEntity.ok().body(ApiResponse.success(userResponse,"유저 인증에 성공했습니다."));
        } catch(Exception e){
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("유저 인증에 실패했습니다."));
        }
    }
}