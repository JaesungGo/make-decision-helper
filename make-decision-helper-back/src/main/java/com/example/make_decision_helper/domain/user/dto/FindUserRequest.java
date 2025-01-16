package com.example.make_decision_helper.domain.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FindUserRequest {
    private String email;

    @Builder
    public FindUserRequest(String email) {
        this.email = email;
    }
}
