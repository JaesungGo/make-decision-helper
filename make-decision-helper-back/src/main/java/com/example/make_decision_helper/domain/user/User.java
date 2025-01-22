package com.example.make_decision_helper.domain.user;

import com.example.make_decision_helper.domain.chatroom.ChatRoom;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="users")
@Getter @Builder
@NoArgsConstructor @AllArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "hostUser")
    private final List<ChatRoom> hostedRooms = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name= "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status{
        ACTIVE,
        INAVTIVE,
        NONE
    }

    public User updateLoginTime() {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .role(this.role)
                .status(Status.ACTIVE)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    public User updateLogoutTime() {
        return User.builder()
                .id(this.id)
                .email(this.email)
                .password(this.password)
                .role(this.role)
                .status(Status.INAVTIVE)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

}
