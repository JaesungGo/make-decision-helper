package com.example.make_decision_helper.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Opinion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long opinionId;

    // 한 방에 의견이 여러개일 수 있다.
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // 유저 한명이 의견이 여러개일 수 있다.
    @ManyToOne
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "create_opinion_at", nullable = false)
    private LocalDateTime createOpinionAt;

}
