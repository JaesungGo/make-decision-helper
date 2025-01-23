package com.example.make_decision_helper.domain.chatuser;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import com.example.make_decision_helper.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    @JoinColumn(name = "chat_room_id")
    ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatUserType type;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected  void onCreate(){
        this.joinedAt = LocalDateTime.now();
    }

    @Builder
    public ChatUser(Long id, ChatRoom chatRoom, User user, String nickname, ChatUserType type) {
        this.id = id;
        this.user = user;
        this.nickname = nickname;
        this.type = type;
        this.chatRoom = chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
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