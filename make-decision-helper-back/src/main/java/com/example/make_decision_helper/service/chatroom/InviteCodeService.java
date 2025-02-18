package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.domain.chatroom.InviteCode;
import com.example.make_decision_helper.domain.user.User;
import com.example.make_decision_helper.repository.chatroom.InviteCodeRepository;
import com.example.make_decision_helper.repository.user.UserRepository;
import com.example.make_decision_helper.util.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final UserRepository userRepository;

    /**
     * 초대 코드 생성
     * @return inviteCode(초대코드)
     */
    @Transactional
    public InviteCode createInviteCode() {
        try {
            String code;
            do {
                code = RandomStringUtils.randomAlphabetic(8);
            } while (inviteCodeRepository.existsByInviteCode(code));

            String userEmail = SecurityUtil.getCurrentUserEmail()
                    .orElseThrow(() -> new RuntimeException("로그인이 필요합니다."));
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            InviteCode inviteCode = InviteCode.builder()
                .inviteCode(code)
                .creatUser(user)
                .build();

            return inviteCodeRepository.save(inviteCode);
        } catch (Exception e) {
            log.error("초대코드 생성 실패: {}", e.getMessage());
            throw new RuntimeException("초대코드 생성에 실패했습니다");
        }
    }
}
