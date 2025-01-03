package com.sparta.chairingproject.config.filter;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.DELETED_USER;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.fcm.sevice.FcmServiceImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.config.JwtUtil;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.auth.dto.request.SigninRequest;
import com.sparta.chairingproject.domain.member.entity.MemberRole;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final JwtUtil jwtUtil;
	private final FcmServiceImpl fcmService;

	private final MemberRepository memberRepository;


	public JwtAuthenticationFilter(JwtUtil jwtUtil, MemberRepository memberRepository,
		FcmServiceImpl fcmService) {
		this.jwtUtil = jwtUtil;
		this.memberRepository = memberRepository;
		this.fcmService = fcmService;
		setFilterProcessesUrl("/auth/signin");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
		HttpServletResponse response) throws
		AuthenticationException {
		log.info("로그인 시도");
		try {
			SigninRequest requestDto = new ObjectMapper().readValue(request.getInputStream(),
				SigninRequest.class);

			Member member = memberRepository.findByEmail(requestDto.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저 이메일입니다."));

			if (member.getDeletedAt() != null) {
				throw new GlobalException(DELETED_USER);
			}

			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(
					requestDto.getEmail(),
					requestDto.getPassword(),
					null
				);

			authToken.setDetails(requestDto.getFcmToken());

			return getAuthenticationManager().authenticate(authToken);

		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request,
		HttpServletResponse response, FilterChain chain, Authentication authResult)
		throws IOException,
		ServletException {
		log.info("로그인 성공 및 JWT 생성");
		MemberRole role = ((UserDetailsImpl) authResult.getPrincipal()).getMember().getMemberRole();

		String token = jwtUtil.createToken(
			((UserDetailsImpl) authResult.getPrincipal()).getMember().getId(),
			((UserDetailsImpl) authResult.getPrincipal()).getMember().getEmail(),
			((UserDetailsImpl) authResult.getPrincipal()).getMember().getImage(),
			role);
		jwtUtil.addJwtToCookie(token, response);

		// FCM 토큰 처리
		String fcmToken = (String) authResult.getDetails();

		if (fcmToken != null && !fcmToken.isEmpty()) {
			fcmService.updateAccessToken(
				((UserDetailsImpl) authResult.getPrincipal()).getMember().getId(),
				fcmToken
			);
		}
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
		HttpServletResponse response, AuthenticationException failed)
		throws IOException, ServletException {
		log.info("로그인 실패");
		response.setStatus(401);
	}
}
