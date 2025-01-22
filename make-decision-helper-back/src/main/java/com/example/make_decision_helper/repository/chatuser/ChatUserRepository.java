package com.example.make_decision_helper.repository.chatuser;

import com.example.make_decision_helper.domain.chatuser.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {


}
