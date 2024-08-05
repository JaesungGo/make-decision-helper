package com.example.make_decision_helper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inviteId;

    @Column(name="invite_code", nullable = false, unique = true)
    private String inviteCode;

    @Column(name="invite_expiry_time",nullable = false)
    private LocalDateTime inviteExpiryTime;

    @ManyToOne
    @JoinColumn(name = "host_user_id",nullable = false)
    private User hostUser;

    @OneToOne(mappedBy = "invite")
    private Room room;


}
