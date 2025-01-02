package com.sparta.chairingproject.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class AuthUtilsTest {

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private AuthUtils authUtils;

	private Member testMember;
	private UserDetailsImpl authUser;

	@BeforeEach
	void setUp() {
		testMember = new Member("Test User", "test@example.com", "encodedPassword", "image",
			MemberRole.USER);
		ReflectionTestUtils.setField(testMember, "id", 1L);
		authUser = new UserDetailsImpl(testMember);
	}

	@Test
	@DisplayName("요청 DTO에 memberId가 null인 경우, authUser에서 Member 반환")
	void findAuthUser_WhenMemberIdIsNull_ReturnsAuthUserMember() {
		// given
		RequestDto req = new RequestDto(null);

		// when
		Member result = authUtils.findAuthUser(req, authUser);

		// then
		assertEquals(testMember, result);
	}

	@Test
	@DisplayName("요청 DTO에 memberId가 0인 경우, authUser에서 Member 반환")
	void findAuthUser_WhenMemberIdIsZero_ReturnsAuthUserMember() {
		// given
		RequestDto req = new RequestDto(0L);

		// when
		Member result = authUtils.findAuthUser(req, authUser);

		// then
		assertEquals(testMember, result);
	}

	@Test
	@DisplayName("요청 DTO의 memberId로 MemberRepository에서 조회 성공")
	void findAuthUser_WhenMemberIdExists_ReturnsMemberFromRepository() {
		// given
		Member repositoryMember = new Member("Repository User", "repo@example.com", "password",
			"image", MemberRole.USER);
		ReflectionTestUtils.setField(testMember, "id", 2L);
		RequestDto req = new RequestDto(2L);

		when(memberRepository.findById(2L)).thenReturn(Optional.of(repositoryMember));

		// when
		Member result = authUtils.findAuthUser(req, authUser);

		// then
		assertEquals(repositoryMember, result);
		verify(memberRepository, times(1)).findById(2L);
	}

	@Test
	@DisplayName("요청 DTO의 memberId로 MemberRepository에서 조회 실패 시 GlobalException 발생")
	void findAuthUser_WhenMemberIdNotFound_ThrowsGlobalException() {
		// given
		RequestDto req = new RequestDto(3L);

		when(memberRepository.findById(3L)).thenReturn(Optional.empty());

		// when & then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> authUtils.findAuthUser(req, authUser));
		assertEquals(ExceptionCode.NOT_FOUND_USER, exception.getExceptionCode());
		verify(memberRepository, times(1)).findById(3L);
	}
}