package com.sparta.chairingproject.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	private String password;

	/* 실제로는 토큰이 없을 시 예외가 발생하고 프론트엔드에 토큰을 다시 요청해야할 것 */
	//@NotEmpty
	private String fcmToken;
}