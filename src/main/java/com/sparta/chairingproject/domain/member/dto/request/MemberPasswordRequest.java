package com.sparta.chairingproject.domain.member.dto.request;

import com.sparta.chairingproject.domain.common.dto.MemberIdDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberPasswordRequest extends MemberIdDto {

    @NotBlank
    private String password;
    @NotBlank
    private String updatePassword;
    @NotBlank
    private String confirmPassword;


}
