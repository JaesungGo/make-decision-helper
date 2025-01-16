package com.example.make_decision_helper.service.user;

import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.domain.user.UserRole;
import com.example.make_decision_helper.domain.user.dto.FindUserRequest;
import com.example.make_decision_helper.domain.user.dto.LoginRequest;
import com.example.make_decision_helper.domain.user.dto.SignUpRequest;
import com.example.make_decision_helper.repository.user.UserRepository;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @InjectMocks UserService userService;

    private User user;
    private String email;
    private String password;
    private String nickname;
    private String encodedPassword;

    @BeforeEach
    void beforeEach() {
        email = "test@test.com";
        password = "password";
        nickname = "nickname";
        encodedPassword = "encodedPassword";

        user = User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("유저 생성 테스트")
    void createUser() {

        //given
        SignUpRequest testSignUpRequest = SignUpRequest.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);


        //when
        userService.createUser(testSignUpRequest);

        //then
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("유저 찾기 테스트")
    void findByEmail() {

        //given
        FindUserRequest testFindUserRequest = FindUserRequest.builder()
                .email(email)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        //when
        User testUser = userService.findByEmail(testFindUserRequest);

        //then
        assertEquals(email,testUser.getEmail());
        verify(userRepository).findByEmail(email);

    }

    @Test
    @DisplayName("로그인 테스트")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest(email, password);
        Map<String, ResponseCookie> cookies = new HashMap<>();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.createTokenAndCookies(email, UserRole.USER)).thenReturn(cookies);

        // when
        Map<String, ResponseCookie> result = userService.login(request);

        // then
        assertEquals(cookies, result);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout() {

        //given
        String accessToken = "validToken";
        Map<String, ResponseCookie> cookies = new HashMap<>();

        when(jwtTokenProvider.getEmailFromToken(accessToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.deleteTokenAndCookies(accessToken)).thenReturn(cookies);

        // when
        Map<String, ResponseCookie> result = userService.logout(accessToken);

        // then
        assertEquals(cookies, result);
        verify(jwtTokenProvider).getEmailFromToken(accessToken);
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
    }
}