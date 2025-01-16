package com.example.make_decision_helper.domain.user.dto;

import com.example.make_decision_helper.domain.user.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class UserResponse {

    private String username;
    private UserRole userRole;

    @Builder
    public UserResponse(String username, UserRole userRole){
        this.username = username;
        this.userRole = userRole;
    }

}
