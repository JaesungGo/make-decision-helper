package com.example.make_decision_helper.repository.chatuser;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.chatroom.dto.RoomResponse;
import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, Long> {
    Optional<ChatUser> findByChatRoomAndUser(ChatRoom chatRoom, User user);
    Optional<ChatUser> findByChatRoomAndNickname(ChatRoom chatRoom, String nickname);
    Optional<ChatUser> findByNickname(String nickname);
    Optional<List<ChatUser>> findByChatRoomOrderByJoinedAtAsc(ChatRoom chatRoom);

    @Query(value = "select new com.example.make_decision_helper.domain.chatroom.dto.RoomResponse(c.id, c.title, c.inviteCode.inviteCode, c.maxParticipants, c.currentParticipants, c.roomStatus) from ChatUser cu inner join cu.chatRoom c where cu.user = :user ")
    List<RoomResponse> findByUser(User user);

}
