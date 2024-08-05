package com.example.make_decision_helper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OpinionReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long reactionId;

    // 하나의 opinion에 여러개의 reaction이 있을 수 있다.
    @ManyToOne
    @JoinColumn(name = "opinion_id", nullable = false)
    private Opinion opinion;

    @OneToOne(mappedBy = "user")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type",nullable = false)
    private ReactionState reactionState;


}
