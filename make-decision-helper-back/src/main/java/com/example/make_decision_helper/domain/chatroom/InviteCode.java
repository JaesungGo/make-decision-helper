package com.example.make_decision_helper.domain.chatroom;

import com.example.make_decision_helper.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "invite_codes")
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "inviteCode", cascade = CascadeType.ALL)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "create_user_id", nullable = false)
    private User creatUser;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @Transient
    private LocalDateTime expirationTime;

}
