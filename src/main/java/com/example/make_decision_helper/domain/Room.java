package com.example.make_decision_helper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @OneToOne
    @JoinColumn(name= "invite_id", nullable = false)
    private Invite invite;

    @OneToMany(mappedBy = "room")
    private Set<UserInRoom> userInRooms;


}
