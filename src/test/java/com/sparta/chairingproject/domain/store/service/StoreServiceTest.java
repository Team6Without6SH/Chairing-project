package com.sparta.chairingproject.domain.store.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.dto.StoreDetailsResponse;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

class StoreServiceTest {

	@InjectMocks
	private StoreService storeService;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private ReviewRepository reviewRepository;

	private Member owner;
	private UserDetailsImpl authMember;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		owner = new Member(1L, "ownerName", "owner@test.com", "password", null);
		authMember = new UserDetailsImpl(owner);
	}

	@Test
	@DisplayName("가게 등록 신청 성공 테스트")
	void registerStore_Success() {

		StoreRequest request = new StoreRequest("Test Store", "Test Address", "010-1234-5678", "Description",
			"testImage");
		when(memberRepository.findById(1L)).thenReturn(Optional.of(owner));
		when(storeRepository.countByOwner(owner)).thenReturn(0); // 이미 등록된 가게가 없으면 0 리턴

		storeService.registerStore(request, authMember);

		verify(storeRepository).save(any(Store.class)); // 가게가 저장된 것을 검증
	}

	@Test
	@DisplayName("가게 등록 실패 테스트 - 이미 등록된 가게가 최대 개수일 경우")
	void registerStore_Fail_MaxStoresReached() {
		// given
		StoreRequest request = new StoreRequest("Test Store", "Test Address", "010-1234-5678", "Description",
			"testImage");
		when(memberRepository.findById(1L)).thenReturn(Optional.of(owner));
		when(storeRepository.countByOwner(owner)).thenReturn(3); // 최대 등록 개수 초과

		// when, then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> storeService.registerStore(request, authMember));
		assertEquals("최대 가게 3개만 소유 가능합니다.", exception.getMessage());
	}

	@Test
	@DisplayName("승인된 가게 목록 조회 성공 테스트")
	void getAllOpenedStores_Success() {

		Store store = new Store(
			1L, "Test Store", "testImage", "Test Description", owner,
			StoreRequestStatus.APPROVED, StoreStatus.OPEN
		);
		when(storeRepository.findAllByRequestStatusAndStatus(
			StoreRequestStatus.APPROVED, StoreStatus.OPEN)
		).thenReturn(List.of(store));

		List<StoreResponse> storeResponses = storeService.getAllOpenedStores();

		assertNotNull(storeResponses);
		assertEquals(1, storeResponses.size());
		assertEquals("Test Store", storeResponses.get(0).getName());
		verify(storeRepository).findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN);
	}

	@Test
	@DisplayName("조회 가능한 가게가 없을 경우 실패 테스트")
	void getAllOpenedStores_Fail_NoStores() {

		when(storeRepository.findAllByRequestStatusAndStatus(
			StoreRequestStatus.APPROVED, StoreStatus.OPEN)
		).thenReturn(List.of());

		GlobalException exception = assertThrows(GlobalException.class, storeService::getAllOpenedStores);

		assertEquals("해당 가게가 없습니다", exception.getMessage());
		verify(storeRepository).findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN);
	}

	@Test
	@DisplayName("가게 정보 수정 성공 테스트")
	void updateStore_Success() {
		// given
		Long storeId = 1L;
		UpdateStoreRequest req = new UpdateStoreRequest("Updated Name", "Updated Address", "Updated Description");
		Store store = new Store(storeId, "Old Name", "Old Image", "Old Description", owner, StoreRequestStatus.APPROVED,
			StoreStatus.OPEN);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(owner));
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		// when
		StoreDetailsResponse response = storeService.updateStore(storeId, req, authMember);

		// then
		assertNotNull(response);
		assertEquals("Updated Name", response.getName());
		assertEquals("Updated Address", response.getAddress());
		assertEquals("Updated Description", response.getDescription());
		verify(storeRepository).save(store);
	}

	@Test
	@DisplayName("가게 정보 수정 실패 테스트 - 사용자 정보 없음")
	void updateStore_Fail_NoUser() {
		// given
		Long storeId = 1L;
		UpdateStoreRequest req = new UpdateStoreRequest("Updated Name", "Updated Address", "Updated Description");
		when(memberRepository.findById(1L)).thenReturn(Optional.empty());

		// when, then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> storeService.updateStore(storeId, req, authMember));

		assertInstanceOf(GlobalException.class, exception);
	}

	@Test
	@DisplayName("가게 정보 수정 실패 테스트 - 가게 정보 없음")
	void updateStore_Fail_NoStore() {
		// given
		Long storeId = 1L;
		UpdateStoreRequest req = new UpdateStoreRequest("Updated Name", "Updated Address", "Updated Description");
		when(memberRepository.findById(1L)).thenReturn(Optional.of(owner));
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// when, then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> storeService.updateStore(storeId, req, authMember));
		assertInstanceOf(GlobalException.class, exception);
	}

	@Test
	@DisplayName("가게 상세 정보 조회 실패 테스트 - 가게 정보 없음")
	void getStoreDetails_Fail_NoStore() {
		// given
		Long storeId = 1L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// when, then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> storeService.getStoreDetails(storeId));

		assertEquals("해당 가게가 없습니다", exception.getMessage());
		verify(storeRepository).findById(storeId);
		verifyNoInteractions(menuRepository);
		verifyNoInteractions(reviewRepository);
	}

}
