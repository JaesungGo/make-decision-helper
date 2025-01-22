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
    @Id @Column(name = "chat_room_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User hostUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_code_id", nullable = false)
    private InviteCode inviteCode;

    @Column(nullable = false)
    private int maxParticipants;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @Enumerated(EnumType.STRING)
    private RoomStatus roomStatus;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatUser> participants = new ArrayList<>();


    public enum RoomStatus{
        ACTIVE, EXPIRED, CLOSED
    }

    @Builder
    public ChatRoom(Long id, String title, User hostUser, InviteCode inviteCode, int maxParticipants, LocalDateTime expirationTime, RoomStatus roomStatus, boolean active, List<ChatUser> participants) {
        this.id = id;
        this.title = title;
        this.hostUser = hostUser;
        this.inviteCode = inviteCode;
        this.maxParticipants = maxParticipants;
        this.expirationTime = expirationTime;
        this.roomStatus = roomStatus;
        this.active = active;
        this.participants = participants;
    }

    /**
     * 채팅방 만료시간 설정 (현재시간 + duration)
     * @param duration
     * @return 만료 시간에 해당하는 LocalDateTime (ex. 2024-10-31-30)
     */
    public static LocalDateTime setExpirationFromDuration(Integer duration) {
        return LocalDateTime.now().plusMinutes(duration);
    }

    public void changeRoomStatus(RoomStatus roomStatus){
        this.roomStatus = roomStatus;
    }
}