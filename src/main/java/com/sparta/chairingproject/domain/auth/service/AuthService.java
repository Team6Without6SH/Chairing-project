package com.sparta.chairingproject.domain.auth.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AmazonS3Client amazonS3Client;
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest, MultipartFile file) {

		if (memberRepository.existsByEmail(signupRequest.getEmail())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
		// 기본 프로필이미지 정해지면 filename에 넣고 주석 삭제예정
		String fileName = null;
		if (!file.isEmpty()) {
			try {
				String uuid = UUID.randomUUID().toString();
				fileName = "userProfile/" + file.getOriginalFilename() + "_" + uuid;
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentType(file.getContentType());
				metadata.setContentLength(file.getSize());
				amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

		MemberRole memberRole = MemberRole.of(signupRequest.getMemberRole());

		Member newMember = new Member(
			signupRequest.getName(),
			signupRequest.getEmail(),
			encodedPassword,
			fileName,
			memberRole
		);
		Member savedUser = memberRepository.save(newMember);

		String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(),
			savedUser.getImage(),
			memberRole);

		return new SignupResponse(bearerToken);
	}
}
