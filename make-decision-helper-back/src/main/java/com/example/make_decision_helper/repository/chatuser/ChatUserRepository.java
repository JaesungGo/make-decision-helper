package com.example.make_decision_helper.repository.chatuser;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    Optional<ChatUser> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    Optional<ChatUser> findByChatRoomAndNickname(ChatRoom chatRoom, String nickname);

}
