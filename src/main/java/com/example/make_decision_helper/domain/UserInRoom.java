package com.example.make_decision_helper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserInRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usersInRoomId;

    @ManyToOne // 여러 유저가 하나의 방에 참여 가능
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nickName;
}
