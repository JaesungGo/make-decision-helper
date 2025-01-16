package com.example.make_decision_helper.domain.user;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    HOST, USER, GUEST;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
