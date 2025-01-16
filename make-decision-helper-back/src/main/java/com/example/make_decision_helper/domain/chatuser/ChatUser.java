package com.example.make_decision_helper.domain.chatuser;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "chat_users",
        indexes = {
        @Index(name = "idx_chat_user_user_id", columnList ="user_id")
        }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // 게스트는 null

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatUserType type;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    // 로그인 사용자용 생성자
    public ChatUser(ChatRoom chatRoom, User user, ChatUserType type) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.nickname = user.getNickname();
        this.type = type;
        this.joinedAt = LocalDateTime.now();
    }

    // 게스트용 생성자
    public static ChatUser createGuest(ChatRoom chatRoom, String nickname) {
        ChatUser participant = new ChatUser();
        participant.chatRoom = chatRoom;
        participant.nickname = nickname;
        participant.type = ChatUserType.GUEST;
        participant.joinedAt = LocalDateTime.now();
        return participant;
    }
}