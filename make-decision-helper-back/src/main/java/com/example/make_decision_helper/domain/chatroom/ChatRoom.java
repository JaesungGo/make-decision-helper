package com.example.make_decision_helper.domain.chatroom;

import com.example.make_decision_helper.domain.chatuser.ChatUser;
import com.example.make_decision_helper.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, nullable = false)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    private String postTemplate;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatUser> participants = new ArrayList<>();

    @Builder
    public ChatRoom(User host, String title, String inviteCode, int maxParticipants,
                    LocalDateTime expirationTime, String postTemplate) {
        this.host = host;
        this.title = title;
        this.inviteCode = inviteCode;
        this.maxParticipants = maxParticipants;
        this.expirationTime = expirationTime;
        this.postTemplate = postTemplate;
    }

}