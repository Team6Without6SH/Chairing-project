package com.sparta.chairingproject.domain.member.dto.request;


import com.sparta.chairingproject.domain.common.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class MemberPasswordRequest extends RequestDto {

    @NotBlank
    private String password;
    @NotBlank
    private String updatePassword;
    @NotBlank
    private String confirmPassword;


}
