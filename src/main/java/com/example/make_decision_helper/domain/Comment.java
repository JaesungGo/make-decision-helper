package com.example.make_decision_helper.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long commentId;

    // 하나의 의견에는 여러개의 코멘트가 달릴 수 있다.
    @ManyToOne
    @JoinColumn(name = "opinion_id",nullable = false)
    private Opinion opinion;

    // 한 명의 회원은 여러개의 코멘트를 달 수 있다.
    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "create_comment_at")
    private LocalDateTime createCommentAt;



}
