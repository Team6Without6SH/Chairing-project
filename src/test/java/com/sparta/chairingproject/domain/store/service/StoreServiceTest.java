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

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@SpringBootTest
class StoreServiceTest {

	@Autowired
	private StoreService storeService;

	@Autowired
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
	@Test
	@DisplayName("사장님 정보 없음")
	void registerStore_Fail_UserNotFound() {
		StoreRequest request = new StoreRequest("Test Store", "123 Test Address", "010-1234-5678", "09:00", "18:00", List.of(), "Description", List.of());

		given(memberRepository.findById(any())).willReturn(Optional.empty());

		GlobalException exception = assertThrows(
			GlobalException.class, () -> storeService.registerStore(request, testUserDetails));

		assertEquals(ExceptionCode.NOT_FOUND_USER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("승인된 가게만 조회")
	void getAllApprovedStores() {
		// given
		Store store1=new Store("가게1","image1.jap","설명",null);
		Store store2 = new Store("가게2", "image2.jpg", "설명2", null);
		Store store3 = new Store("가게3", "image3.jpg", "설명3", null);

		store1.updateStoreStatus(StoreStatus.APPROVED); // 가게1 승인
		storeRepository.save(store1);
		storeRepository.save(store2);
		storeRepository.save(store3);

		// When: 승인된 가게만 조회
		List<StoreResponse> stores = storeService.getAllApprovedStores();

		// Then: 승인된 가게만 포함되어야 함
		assertEquals(1, stores.size());
		assertEquals("가게1", stores.get(0).getName());
	}
}
