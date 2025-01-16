package com.example.make_decision_helper.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {

    @Email
    private String email;

    @Pattern(regexp = "^[A-Za-z0-9]+$")
    private String password;

    @Size(min=2, max=10, message="닉네임은 2자 이상 10자 이하여야 합니다.")
    private String nickname;

    @Builder
    public SignUpRequest(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

}
