package com.example.make_decision_helper.service.user;

import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.domain.user.UserRole;
import com.example.make_decision_helper.domain.user.dto.FindUserRequest;
import com.example.make_decision_helper.domain.user.dto.LoginRequest;
import com.example.make_decision_helper.domain.user.dto.SignUpRequest;
import com.example.make_decision_helper.domain.user.dto.UserResponse;
import com.example.make_decision_helper.repository.user.UserRepository;
import com.example.make_decision_helper.util.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service @Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    /**
     * 회원가입 하기
     * @param signUpRequest
     */
    public void createUser(SignUpRequest signUpRequest){

        // 이메일 중복 체크
        if(userRepository.existsByEmail(signUpRequest.getEmail())){
            throw new RuntimeException("이메일이 이미 존재합니다.");
        }

        // 유저 객체 생성 및 저장
        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .nickname(signUpRequest.getNickname())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }

    /**
     * 이메일 기반으로 유저 찾기
     * @param findUserRequest
     * @return 찾은 유저 정보 혹은 예외
     */
    public User findByEmail(FindUserRequest findUserRequest){
        return userRepository.findByEmail(findUserRequest.getEmail())
                .orElseThrow(()-> new RuntimeException("해당 유저를 찾을 수 없습니다."));
    }


    /**
     * 로그인 로직
     * @param loginRequest(email,password)
     * @return AccessToken, RefreshToken 값이 담긴 HashMap<String,ResponseCookie>
     */
    @Transactional
    public Map<String, ResponseCookie> login(LoginRequest loginRequest){

        User findUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        if(!passwordEncoder.matches(loginRequest.getPassword(), findUser.getPassword())){
            throw new RuntimeException("비밀 번호가 일치하지 않습니다.");
        }

        User updatedUser = findUser.updateLoginTime();
        userRepository.save(updatedUser);

        return jwtTokenProvider.createTokenAndCookies(loginRequest.getEmail(), UserRole.USER);

    }

    /**
     * 로그아웃 로직
     * @param accessToken
     * @return AccessToken, RefreshToken의 만료된 값이 담긴 HashMap<String,ResponseCookie>
     */
    @Transactional
    public Map<String, ResponseCookie> logout(String accessToken) {

        if (accessToken == null) {
            throw new RuntimeException("접근 불가능한 토큰입니다.");
        }

        String userEmail = jwtTokenProvider.getEmailFromToken(accessToken);

        User findUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));


        User updatedUser = findUser.updateLogoutTime();
        userRepository.save(updatedUser);

        return jwtTokenProvider.deleteTokenAndCookies(accessToken);
    }

    /**
     * 프론트에서 유저 인증 요청 시 유저 정보 반환
     * @param userDetails
     * @return UserResponse(username, userRole)
     */
    @Transactional(readOnly = true)
    public UserResponse authUser(UserDetails userDetails){
        return UserResponse.builder()
                .username(userDetails.getUsername())
                .userRole(UserRole.valueOf(userDetails.getAuthorities().stream()
                                .findFirst()
                                .map(GrantedAuthority::getAuthority)
                                .map(authority -> authority.replace("ROLE_", ""))
                                .orElseThrow(()->new IllegalStateException("권한을 찾을 수 없습니다."))))
                        .build();
    }

}
