package com.sparta.chairingproject.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberPasswordRequest {

    @NotBlank
    private String password;
    @NotBlank
    private String updatePassword;
    @NotBlank
    private String confirmPassword;


}
