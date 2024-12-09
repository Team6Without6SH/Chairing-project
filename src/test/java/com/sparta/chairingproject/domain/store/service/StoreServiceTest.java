package com.sparta.chairingproject.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@SpringBootTest
class StoreServiceTest {

	@Autowired
	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private MemberRepository memberRepository;

	private Member testMember;
	private UserDetailsImpl testUserDetails;

	@BeforeEach
	void setup() {
		testMember = new Member("Test Name", "test@test.com", "password", MemberRole.OWNER);
		testUserDetails = new UserDetailsImpl(testMember);
	}

	@Test
	@DisplayName("가게 등록 성공 테스트")
	void registerStore_Success() {
		// Given
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
		then(storeRepository).should(times(1)).save(Mockito.any());
	}

	@Test
	@DisplayName("이미 등록한 가게 실패 테스트")
	void registerStore_Fail_ExistingStore() {
		// Given
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
		GlobalException exception = assertThrows(
			GlobalException.class,
			() -> storeService.registerStore(request, testUserDetails)
		);

		assertEquals(ExceptionCode.CANNOT_EXCEED_STORE_LIMIT, exception.getExceptionCode());
	}

	@Test
	@DisplayName("영업 시간 누락 실패 테스트")
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
		GlobalException exception = assertThrows(
			GlobalException.class,
			() -> storeService.registerStore(request, testUserDetails)
		);

		assertEquals(ExceptionCode.STORE_CLOSED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("사장님 정보 없음 실패 테스트")
	void registerStore_Fail_UserNotFound() {
		// Given
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

		given(memberRepository.findById(any())).willReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(
			GlobalException.class,
			() -> storeService.registerStore(request, testUserDetails)
		);

		assertEquals(ExceptionCode.NOT_FOUND_USER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("승인된 가게만 조회 테스트")
	void getAllOpenedStores_Success() {
		// Given
		Store store1 = new Store("가게1", "image1.jpg", "설명1", testMember);
		Store store2 = new Store("가게2", "image2.jpg", "설명2", testMember);

		store1.updateStoreStatus(StoreStatus.OPEN);
		store1.updateRequestStatus(StoreRequestStatus.APPROVED);
		store2.updateStoreStatus(StoreStatus.CLOSED);

		given(storeRepository.findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN))
			.willReturn(List.of(store1));

		// When
		List<StoreResponse> stores = storeService.getAllOpenedStores();

		// Then
		assertEquals(1, stores.size());
		assertEquals("가게1", stores.get(0).getName());
		assertEquals("image1.jpg", stores.get(0).getImages().get(0));
		assertEquals("설명1", stores.get(0).getDescription());
	}

	@Test
	@DisplayName("조회 가능한 가게가 없을 경우 실패 테스트")
	void getAllOpenedStores_NoStores() {
		// Given
		given(storeRepository.findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN))
			.willReturn(List.of());

		// When & Then
		GlobalException exception = assertThrows(
			GlobalException.class,
			() -> storeService.getAllOpenedStores()
		);

		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}
}
