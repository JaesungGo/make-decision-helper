package com.example.make_decision_helper.repository.chatroom;

import com.example.make_decision_helper.domain.chatroom.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface InviteCodeRepository extends JpaRepository<InviteCode,Long> {

//    @Query("select i from InviteCode i where i.creatUser.id = :userId ")
//    Optional<InviteCode> findByUserId(@Param("userId") Long userId);

    Optional<InviteCode> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String code);
}
