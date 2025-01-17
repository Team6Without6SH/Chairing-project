package com.sparta.chairingproject.domain.member.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckPasswordRequest extends RequestDto {

	@NotBlank
	private String password;

}