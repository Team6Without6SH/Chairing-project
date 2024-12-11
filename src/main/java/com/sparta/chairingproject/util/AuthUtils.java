package com.sparta.chairingproject.util;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.stereotype.Component;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class AuthUtils {

	private final MemberRepository memberRepository;

	public Member findAuthUser(RequestDto req, UserDetailsImpl authUser) {
		if (req.getMemberId() == null || req.getMemberId() == 0) {
			return authUser.getMember();
		} else {
			return memberRepository.findById(req.getMemberId()).orElseThrow(
				() -> new GlobalException(NOT_FOUND_USER)
			);
		}
	}
}
