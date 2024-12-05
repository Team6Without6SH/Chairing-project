package com.sparta.chairingproject.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@SpringBootTest
class StoreServiceTest {
	@Autowired
	private StoreService storeService;

	@MockBean
	private StoreRepository storeRepository;

	@MockBean
	private MemberRepository memberRepository;

	private Member testMember;
	private UserDetailsImpl testUserDetails;

	@BeforeEach
	void setup() {
		testMember =new Member ("Test Name", "test@test.com", "password", MemberRole.USER);
		testUserDetails = new UserDetailsImpl(testMember);
}


	@Test
	@DisplayName("등록 성공")
	void registerStore_Success() {
		//Given
		StoreRequest request = new StoreRequest(
			"Test Store",
			"123 Test Address",
			"010-1234-5678",
			"09:00",
			"18:00",
			List.of("Category1", "Category2"),
			"Test Description",
			List.of("image1.jpg", "image2.jpg")
		);

		given(memberRepository.findById(testMember.getId())).willReturn(Optional.of(testMember));
		given(storeRepository.existsByOwner(testMember)).willReturn(false);

		// When
		storeService.registerStore(request, testUserDetails);

		// Then
		then(storeRepository).should().save(Mockito.any());
	}

	@Test
	@DisplayName("이미 등록한 가게")
	void registerStore_Fail_ExistingStore() {
		//Given
		StoreRequest request = new StoreRequest(
			"Test Store",
			"123 Test Address",
			"010-1234-5678",
			"09:00",
			"18:00",
			List.of("Category1", "Category2"),
			"Test Description",
			List.of("image1.jpg", "image2.jpg")
		);

		given(memberRepository.findById(testMember.getId())).willReturn(Optional.of(testMember));
		given(storeRepository.existsByOwner(testMember)).willReturn(true);

		// When & Then
		IllegalStateException exception = assertThrows(
			IllegalStateException.class,
			() -> storeService.registerStore(request, testUserDetails)
		);

		// Assertion
		assertThrows(IllegalStateException.class, () -> {
			storeService.registerStore(request, testUserDetails);
		});
	}

	@Test
	@DisplayName("영업 시간 누락")
	void registerStore_Fail_InvalidOpenCloseTime() {
		// Given
		StoreRequest request = new StoreRequest(
			"Test Store",
			"123 Test Address",
			"010-1234-5678",
			"09:00", // openTime
			null,   // closeTime 누락
			List.of("Category1", "Category2"),
			"Test Description",
			List.of("image1.jpg", "image2.jpg")
		);

		given(memberRepository.findById(testMember.getId())).willReturn(Optional.of(testMember));
		given(storeRepository.existsByOwner(testMember)).willReturn(false);

		// When & Then
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> storeService.registerStore(request, testUserDetails)
		);

		// Assertion
		assertThrows(IllegalArgumentException.class, () -> {
			storeService.registerStore(request, testUserDetails);
		});
	}
}
