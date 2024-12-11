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
import org.springframework.test.util.ReflectionTestUtils;

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

	private Store store1;
	private Store store2;
	private Store store3;
	private Store store4;
	private Store store5;
	private Store store6;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		Member owner1 = new Member("Owner 1", "owner1@example.com", "password123", MemberRole.OWNER);
		ReflectionTestUtils.setField(owner1, "id", 1L);

		Member owner2 = new Member("Owner 2", "owner2@example.com", "password456", MemberRole.OWNER);
		ReflectionTestUtils.setField(owner2, "id", 2L);

		//초기
		store1 = new Store("Test Store 1", "test_image_1.jpg", "Description 1", "Seoul", owner1);
		ReflectionTestUtils.setField(store1, "id", 1L);
		ReflectionTestUtils.setField(store1, "status", StoreStatus.OPEN);
		ReflectionTestUtils.setField(store1, "requestStatus", StoreRequestStatus.PENDING);

		//승인
		store2 = new Store("Test Store 2", "test_image_2.jpg", "Description 2", "Busan", owner2);
		ReflectionTestUtils.setField(store2, "id", 2L);
		ReflectionTestUtils.setField(store2, "status", StoreStatus.CLOSED);
		ReflectionTestUtils.setField(store2, "requestStatus", StoreRequestStatus.APPROVED);

		// 삭제 요청 상태
		store3 = new Store("Test Store 3", "test_image_3.jpg", "Description 3", "Incheon", owner1);
		ReflectionTestUtils.setField(store3, "id", 3L);
		ReflectionTestUtils.setField(store3, "status", StoreStatus.CLOSED);
		ReflectionTestUtils.setField(store3, "requestStatus", StoreRequestStatus.DELETE_REQUESTED);

		// 삭제 완료 상태
		store4 = new Store("Test Store 4", "test_image_4.jpg", "Description 4", "Daegu", owner2);
		ReflectionTestUtils.setField(store4, "id", 4L);
		ReflectionTestUtils.setField(store4, "status", StoreStatus.CLOSED);
		ReflectionTestUtils.setField(store4, "requestStatus", StoreRequestStatus.DELETED);

		// 삭제 거부 상태
		store5 = new Store("Test Store 5", "test_image_5.jpg", "Description 5", "Gwangju", owner1);
		ReflectionTestUtils.setField(store5, "id", 5L);
		ReflectionTestUtils.setField(store5, "status", StoreStatus.OPEN);
		ReflectionTestUtils.setField(store5, "requestStatus", StoreRequestStatus.DELETE_REJECTED);

		// 거부된 등록 요청 상태
		store6 = new Store("Test Store 6", "test_image_6.jpg", "Description 6", "Ulsan", owner2);
		ReflectionTestUtils.setField(store6, "id", 6L);
		ReflectionTestUtils.setField(store6, "status", StoreStatus.PENDING);
		ReflectionTestUtils.setField(store6, "requestStatus", StoreRequestStatus.REJECTED);
	}

	@Test
	@DisplayName("관리자_모든가게 조회 성공")
	void getAllStores_success() {

		when(storeRepository.findAll()).thenReturn(List.of(store1, store2));

		List<StoreAdminResponse> response = adminStoreService.getAllStores();

		assertEquals(2, response.size());
		assertEquals("Test Store 1", response.get(0).getName());
		assertEquals("Test Store 2", response.get(1).getName());
		verify(storeRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("관리자_모든 가게 조회 실패: 가게 없음")
	void getAllStores_notFound() {

		when(storeRepository.findAll()).thenReturn(List.of());

		GlobalException exception = assertThrows(GlobalException.class, () -> adminStoreService.getAllStores());
		assertEquals("NOT_FOUND_STORE", exception.getExceptionCode().name());
		verify(storeRepository, times(1)).findAll();
	}

	@Test
	@DisplayName("단일 가게 조회 성공")
	void getStoreById_success() {

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		StoreAdminResponse response = adminStoreService.getStoreById(1L);

		assertNotNull(response);
		assertEquals("Test Store 1", response.getName());
		verify(storeRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("단일 가게 조회 실패 (가게 없음)")
	void getStoreById_notFound() {

		when(storeRepository.findById(1L)).thenReturn(Optional.empty());

		GlobalException exception = assertThrows(GlobalException.class, () -> adminStoreService.getStoreById(1L));
		assertEquals("NOT_FOUND_STORE", exception.getExceptionCode().name());
		verify(storeRepository, times(1)).findById(1L);
	}

	@Test
	@DisplayName("가게 상태 업데이트 성공")
	void updateStoreRequestStatus_success() {

		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
		ReflectionTestUtils.setField(request, "storeId", 1L);
		ReflectionTestUtils.setField(request, "status", "APPROVED");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		StoreAdminResponse response = adminStoreService.updateStoreRequestStatus(request);

		assertNotNull(response);
		assertEquals("APPROVED", response.getRequestStatus());
		verify(storeRepository, times(1)).save(store1);
	}

	@Test
	@DisplayName("가게 상태 업데이트 실패 (잘못된 상태)")
	void updateStoreRequestStatus_invalidStatus() {

		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
		ReflectionTestUtils.setField(request, "storeId", 1L);
		ReflectionTestUtils.setField(request, "status", "INVALID_STATUS");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> adminStoreService.updateStoreRequestStatus(request));
		assertEquals("INVALID_REQUEST_STATUS", exception.getExceptionCode().name());
	}

	@Test
	@DisplayName("가게 상태 업데이트 성공 (REJECTED 상태)")
	void updateStoreRequestStatus_rejected() {

		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
		ReflectionTestUtils.setField(request, "storeId", 1L);
		ReflectionTestUtils.setField(request, "status", "REJECTED");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		StoreAdminResponse response = adminStoreService.updateStoreRequestStatus(request);

		assertNotNull(response);
		assertEquals(StoreRequestStatus.REJECTED.name(), store1.getRequestStatus().name());
		assertEquals("REJECTED", response.getRequestStatus());
		verify(storeRepository, times(1)).save(store1);
	}

	@Test
	@DisplayName("가게 상태 업데이트 실패 (DELETE_REQUESTED 상태)")
	void updateStoreRequestStatus_deleteRequested() {

		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
		ReflectionTestUtils.setField(request, "storeId", 1L);
		ReflectionTestUtils.setField(request, "status", "DELETE_REQUESTED");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> adminStoreService.updateStoreRequestStatus(request));
		assertEquals("INVALID_REQUEST_STATUS", exception.getExceptionCode().name());
		verify(storeRepository, times(0)).save(store1);
	}

	@Test
	@DisplayName("가게 상태 업데이트 성공 (DELETED 상태)")
	void updateStoreRequestStatus_deleted() {

		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
		ReflectionTestUtils.setField(request, "storeId", 1L);
		ReflectionTestUtils.setField(request, "status", "DELETED");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		StoreAdminResponse response = adminStoreService.updateStoreRequestStatus(request);

		assertNotNull(response);
		assertEquals("DELETED", response.getRequestStatus());
		verify(storeRepository, times(1)).save(store1);
	}

	@Test
	@DisplayName("가게 상태 업데이트 성공 (DELETE_REJECTED 상태)")
	void updateStoreRequestStatus_deleteRejected() {

		UpdateStoreStatusRequest request = new UpdateStoreStatusRequest();
		ReflectionTestUtils.setField(request, "storeId", 1L);
		ReflectionTestUtils.setField(request, "status", "DELETE_REJECTED");

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		StoreAdminResponse response = adminStoreService.updateStoreRequestStatus(request);

		assertNotNull(response);
		assertEquals(StoreRequestStatus.DELETE_REJECTED.name(), store1.getRequestStatus().name()); // 엔티티 상태 확인
		assertEquals("DELETE_REJECTED", response.getRequestStatus());
		verify(storeRepository, times(1)).save(store1);
	}

	@Test
	@DisplayName("가게 삭제 승인 성공")
	void approveCloseStore_success() {

		ReflectionTestUtils.setField(store1, "requestStatus", StoreRequestStatus.DELETE_REQUESTED);
		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		adminStoreService.approveCloseStore(1L);

		verify(storeRepository, times(1)).save(store1);
		assertEquals(StoreStatus.CLOSED, store1.getStatus());
		assertEquals(StoreRequestStatus.DELETED, store1.getRequestStatus());
	}

	@Test
	@DisplayName("가게 삭제 승인 실패 (잘못된 상태)")
	void approveCloseStore_invalidStatus() {

		when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));

		GlobalException exception = assertThrows(GlobalException.class, () -> adminStoreService.approveCloseStore(1L));
		assertEquals("INVALID_REQUEST_STATUS", exception.getExceptionCode().name());
	}

}
