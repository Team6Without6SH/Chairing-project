package com.sparta.chairingproject.domain.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.store.dto.StoreAdminResponse;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreStatusRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

class AdminStoreServiceTest {

	@InjectMocks
	private AdminStoreService adminStoreService;

	@Mock
	private StoreRepository storeRepository;

	private Store store;
	private Member member;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		member = new Member("Test user", "test@example.com", "1234", MemberRole.USER);
		store = new Store(1L, "Test store", "Test image", "Test description", member);
		store.updateStoreStatus(StoreStatus.OPEN);
	}

	@Test
	@DisplayName("모든 가게 조회 성공 테스트")
	void getAllStores_Success() {
		when(storeRepository.findAll()).thenReturn(List.of(store));

		List<StoreAdminResponse> stores = adminStoreService.getAllStores();

		assertNotNull(stores);
		assertEquals(1, stores.size());
		assertEquals("Test store", stores.get(0).getName());
		verify(storeRepository).findAll();
	}

	@Test
	@DisplayName("단일 가게 조회 성공 테스트")
	void getStoresById_Success() {
		when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

		StoreAdminResponse response = adminStoreService.getStoreById(1L);

		assertNotNull(response);
		assertEquals("Test store", response.getName());
		verify(storeRepository).findById(1L);
	}

	@Test
	@DisplayName("단일 가게 조회 실패 테스트 - 잘못된 ID")
	void getStoreById_Fail_InvalidId() {
		when(storeRepository.findById(1L)).thenReturn(java.util.Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class, () -> adminStoreService.getStoreById(1L));

		assertEquals("해당 가게가 없습니다", exception.getMessage());
		verify(storeRepository).findById(1L);
	}

	@Test
	@DisplayName("가게 상태 변경 성공 테스트 - 승인")
	void updateStoreRequestStatus_Success_Approve() {
		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest(1L, "APPROVED");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

		StoreAdminResponse response = adminStoreService.updateStoreRequestStatus(request);

		assertNotNull(response);
		assertEquals("OPEN", response.getStatus());
		assertEquals(StoreRequestStatus.APPROVED, store.getRequestStatus());
		verify(storeRepository).findById(1L);
		verify(storeRepository).save(store);
	}

	@Test
	@DisplayName("가게 상태 변경 성공 테스트 - 거절")
	void updateStoreRequestStatus_Success_Reject() {
		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest(1L, "REJECTED");
		when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

		StoreAdminResponse response = adminStoreService.updateStoreRequestStatus(request);

		assertNotNull(response);
		assertEquals("PENDING", response.getStatus());
		assertEquals(StoreRequestStatus.REJECTED, store.getRequestStatus());
		verify(storeRepository).findById(1L);
		verify(storeRepository).save(store);
	}

	@Test
	@DisplayName("가게 상태 변경 실패 테스트 - 잘못된 상태 값")
	void updateStoreRequestStatus_Fail_InvalidStatus() {
		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest(1L, "INVALID_STATUS");
		when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> adminStoreService.updateStoreRequestStatus(request));

		assertEquals("유효하지 않은 요청 상태입니다", exception.getMessage());
		verify(storeRepository).findById(1L);
	}

	@Test
	@DisplayName("가게 삭제 성공 테스트")
	void approveCloseStore_Success() {
		store.markAsDeleted();
		when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));

		adminStoreService.approveCloseStore(1L);

		verify(storeRepository).softDeleteById(1L);
	}

	@Test
	@DisplayName("가게 삭제 실패 테스트 - 잘못된 ID")
	void approveCloseStore_Fail_InvalidId() {
		when(storeRepository.findById(1L)).thenReturn(java.util.Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class, () -> adminStoreService.approveCloseStore(1L));

		assertEquals("해당 가게가 없습니다", exception.getMessage());
		verify(storeRepository).findById(1L);
	}

	@Test
	@DisplayName("가게 삭제 실패 테스트 - 이미 삭제된 가게")
	void approveCloseStore_Fail_AlreadyDeleted() {
		store.inActive(false);
		when(storeRepository.findById(1L)).thenReturn(java.util.Optional.of(store));

		GlobalException exception = assertThrows(GlobalException.class, () -> adminStoreService.approveCloseStore(1L));

		assertEquals("이미 삭제된 가게입니다.", exception.getMessage());
		verify(storeRepository).findById(1L);
	}
}
