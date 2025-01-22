package com.example.make_decision_helper.service.chatroom;

import com.example.make_decision_helper.repository.chatroom.InviteCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InviteCodeService {

    private InviteCodeRepository inviteCodeRepository;

    /**
     * 초대 코드 생성
     * @return inviteCode(초대코드)
     */
    @Transactional
    public String createInviteCode(){
        try{
            String inviteCode;
            do{
                inviteCode = RandomStringUtils.randomAlphabetic(8);
            } while(inviteCodeRepository.existsByInviteCode(inviteCode));
            return inviteCode;
        } catch (Exception e){
            log.error("초대코드 생성 실패");
            return null;
        }
    }
}
