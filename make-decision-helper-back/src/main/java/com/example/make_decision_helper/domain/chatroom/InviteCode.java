package com.example.make_decision_helper.domain.chatroom;

import com.example.make_decision_helper.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "invite_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "inviteCode")
    ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_user_id", nullable = false)
    private User creatUser;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @Transient
    private LocalDateTime expirationTime;

    @Builder
    public InviteCode(Long id, ChatRoom chatRoom, User creatUser, String inviteCode, LocalDateTime expirationTime) {
        this.id = id;
        this.creatUser = creatUser;
        this.inviteCode = inviteCode;
        this.expirationTime = expirationTime;
        this.chatRoom = chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom){
        this.chatRoom = chatRoom;
    }
}
