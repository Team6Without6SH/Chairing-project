package com.sparta.chairingproject.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.domain.auth.dto.request.SignupRequest;
import com.sparta.chairingproject.domain.auth.dto.response.SignupResponse;
import com.sparta.chairingproject.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/auth/signup")
	public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
		return authService.signup(signupRequest);
	}
}
