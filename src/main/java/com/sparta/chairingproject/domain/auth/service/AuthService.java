package com.sparta.chairingproject.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.JwtUtil;
import com.sparta.chairingproject.domain.auth.dto.request.SignupRequest;
import com.sparta.chairingproject.domain.auth.dto.response.SignupResponse;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {

		if (memberRepository.existsByEmail(signupRequest.getEmail())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

		MemberRole memberRole = MemberRole.of(signupRequest.getMemberRole());

		Member newMember = new Member(
			signupRequest.getName(),
			signupRequest.getEmail(),
			encodedPassword,
			memberRole
		);
		Member savedUser = memberRepository.save(newMember);

		String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), memberRole);

		return new SignupResponse(bearerToken);
	}
}
