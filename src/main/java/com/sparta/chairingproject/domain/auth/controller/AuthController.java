package com.sparta.chairingproject.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.domain.auth.dto.request.SignupRequest;
import com.sparta.chairingproject.domain.auth.dto.response.SignupResponse;
import com.sparta.chairingproject.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/auth/signup")
	public SignupResponse signup(
		@Valid @RequestPart(value = "signupRequest") SignupRequest signupRequest,
		@RequestPart(value = "profile", required = false) MultipartFile file) {
		return authService.signup(signupRequest, file);
	}
}
