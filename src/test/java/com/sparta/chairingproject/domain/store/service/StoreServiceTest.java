//package com.sparta.chairingproject.domain.store.service;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import com.sparta.chairingproject.config.exception.customException.GlobalException;
//import com.sparta.chairingproject.config.security.UserDetailsImpl;
//import com.sparta.chairingproject.domain.member.entity.Member;
//import com.sparta.chairingproject.domain.member.entity.MemberRole;
//import com.sparta.chairingproject.domain.member.repository.MemberRepository;
//import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
//import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
//import com.sparta.chairingproject.domain.store.dto.StoreDetailsResponse;
//import com.sparta.chairingproject.domain.store.dto.StoreOwnerResponse;
//import com.sparta.chairingproject.domain.store.dto.StoreRequest;
//import com.sparta.chairingproject.domain.store.dto.StoreResponse;
//import com.sparta.chairingproject.domain.store.dto.UpdateStoreRequest;
//import com.sparta.chairingproject.domain.store.entity.Store;
//import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
//import com.sparta.chairingproject.domain.store.entity.StoreStatus;
//import com.sparta.chairingproject.domain.store.repository.StoreRepository;
//
//class StoreServiceTest {
//
//	@InjectMocks
//	private StoreService storeService;
//
//	@Mock
//	private StoreRepository storeRepository;
//
//	@Mock
//	private MemberRepository memberRepository;
//
//	@Mock
//	private MenuRepository menuRepository;
//
//	@Mock
//	private ReviewRepository reviewRepository;
//
//	private Member owner;
//	private UserDetailsImpl authUser;
//	private Store store;
//	private StoreRequest storeRequest;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//		storeRequest = new StoreRequest();
//
//		owner = new Member("Test owner", "owner@test.com", "password123", "image",MemberRole.OWNER);
//		authUser = new UserDetailsImpl(owner);
//		store = new Store("Test Store", "Test Address", "Test Image", "Test Description", owner);
//		ReflectionTestUtils.setField(store, "id", 1L);
//
//		storeRequest = new StoreRequest();
//		ReflectionTestUtils.setField(storeRequest, "name", "Test Store");
//		ReflectionTestUtils.setField(storeRequest, "address", "Test Address");
//		ReflectionTestUtils.setField(storeRequest, "phone", "010-1234-5678");
//		ReflectionTestUtils.setField(storeRequest, "openTime", "09:00");
//		ReflectionTestUtils.setField(storeRequest, "closeTime", "18:00");
//		ReflectionTestUtils.setField(storeRequest, "category", "Test Category");
//		ReflectionTestUtils.setField(storeRequest, "description", "This is a test description");
//		ReflectionTestUtils.setField(storeRequest, "image", "testImage.jpg");
//	}
//
//	@Test
//	@DisplayName("가게 등록 신청 성공 테스트")
//	void registerStore_Success() {
//
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.countByOwner(owner)).thenReturn(0);
//
//		storeService.registerStore(storeRequest, authUser);
//
//		verify(storeRepository, times(1)).save(any(Store.class));
//	}
//
//	@Test
//	@DisplayName("가게 등록 실패 테스트 - 이미 등록된 가게가 최대 개수일 경우")
//	void registerStore_Fail_MaxStoresReached() {
//
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.countByOwner(owner)).thenReturn(3);
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.registerStore(storeRequest, authUser));
//		assertEquals("최대 가게 3개만 소유 가능합니다.", exception.getMessage());
//	}
//
//	@Test
//	@DisplayName("가게 등록 실패 테스트 - 잘못된 영업시간 입력")
//	void registerStore_Fail_InvalidOpenCloseTime() {
//		ReflectionTestUtils.setField(storeRequest, "openTime", "09:00");
//		ReflectionTestUtils.setField(storeRequest, "closeTime", null); // 잘못된 입력
//
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.countByOwner(owner)).thenReturn(0);
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.registerStore(storeRequest, authUser));
//		assertEquals("영업 시간이 아닙니다", exception.getMessage());
//	}
//
//	@Test
//	@DisplayName("가게 조회 실패 테스트 - 가게 운영 중지")
//	void getStoreById_Fail_StoreOutOfBusiness() {
//
//		ReflectionTestUtils.setField(store, "requestStatus", StoreRequestStatus.REJECTED); // APPROVED가 아님
//		ReflectionTestUtils.setField(store, "status", StoreStatus.CLOSED); // PENDING이 아님
//
//		when(storeRepository.findByIdAndOwnerId(store.getId(), owner.getId())).thenReturn(Optional.of(store));
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.getStoreById(store.getId(), owner.getId()));
//
//		assertEquals("폐업한 가게입니다", exception.getMessage());
//	}
//
//	@Test
//	@DisplayName("승인된 가게 목록 조회 성공 테스트")
//	void getAllOpenedStores_Success() {
//
//		when(storeRepository.findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN)).thenReturn(
//			List.of(store));
//
//		List<StoreResponse> storeResponses = storeService.getAllOpenedStores();
//
//		assertNotNull(storeResponses);
//		assertEquals(1, storeResponses.size());
//		assertEquals("Test Store", storeResponses.get(0).getName());
//		verify(storeRepository).findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN);
//	}
//
//	@Test
//	@DisplayName("가게 상세 조회 성공 테스트")
//	void getStoreDetails_Success() {
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.findByStoreId(store.getId())).thenReturn(List.of());
//		when(reviewRepository.findByStoreId(store.getId())).thenReturn(List.of());
//
//		StoreDetailsResponse storeDetailsResponse = storeService.getStoreDetails(store.getId());
//
//		assertNotNull(storeDetailsResponse);
//		assertEquals("Test Store", storeDetailsResponse.getName());
//	}
//
//	@Test
//	@DisplayName("조회 가능한 가게가 없을 경우 실패 테스트")
//	void getAllOpenedStores_Fail_NoStores() {
//
//		when(storeRepository.findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN)).thenReturn(
//			List.of());
//
//		GlobalException exception = assertThrows(GlobalException.class, storeService::getAllOpenedStores);
//
//		assertEquals("해당 가게가 없습니다", exception.getMessage());
//		verify(storeRepository).findAllByRequestStatusAndStatus(StoreRequestStatus.APPROVED, StoreStatus.OPEN);
//	}
//
//	@Test
//	@DisplayName("가게 정보 수정 성공 테스트")
//	void updateStore_Success() {
//
//		UpdateStoreRequest request = new UpdateStoreRequest();
//		ReflectionTestUtils.setField(request, "name", "Updated Name");
//		ReflectionTestUtils.setField(request, "address", "Updated Address");
//		ReflectionTestUtils.setField(request, "description", "Updated Description");
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//
//		StoreDetailsResponse response = storeService.updateStore(store.getId(), request, authUser);
//
//		assertNotNull(response);
//		assertEquals("Updated Name", response.getName());
//	}
//
//	@Test
//	@DisplayName("가게 정보 수정 실패 테스트 - 사용자 정보 없음")
//	void updateStore_Fail_NoUser() {
//
//		UpdateStoreRequest request = new UpdateStoreRequest();
//		ReflectionTestUtils.setField(request, "name", "Updated Name");
//		ReflectionTestUtils.setField(request, "address", "Updated Address");
//		ReflectionTestUtils.setField(request, "description", "Updated Description");
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.empty());
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.updateStore(store.getId(), request, authUser));
//
//		assertInstanceOf(GlobalException.class, exception);
//	}
//
//	@Test
//	@DisplayName("가게 정보 수정 실패 테스트 - 가게 정보 없음")
//	void updateStore_Fail_NoStore() {
//
//		UpdateStoreRequest request = new UpdateStoreRequest();
//		ReflectionTestUtils.setField(request, "name", "Updated Name");
//		ReflectionTestUtils.setField(request, "address", "Updated Address");
//		ReflectionTestUtils.setField(request, "description", "Updated Description");
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.updateStore(store.getId(), request, authUser));
//		assertInstanceOf(GlobalException.class, exception);
//	}
//
//	@Test
//	@DisplayName("가게 상세 정보 조회 실패 테스트 - 가게 정보 없음")
//	void getStoreDetails_Fail_NoStore() {
//
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.getStoreDetails(store.getId()));
//
//		assertEquals("해당 가게가 없습니다", exception.getMessage());
//		verify(storeRepository).findById(store.getId());
//		verifyNoInteractions(menuRepository);
//		verifyNoInteractions(reviewRepository);
//	}
//
//	@Test
//	@DisplayName("가게 삭제 요청 성공 테스트")
//	void requestDeleteStore_Success() {
//
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.findByIdAndOwnerId(store.getId(), owner.getId())).thenReturn(Optional.of(store));
//
//		storeService.requestDeleteStore(store.getId(), authUser);
//
//		verify(storeRepository, times(1)).save(store);
//		assertEquals(StoreRequestStatus.DELETE_REQUESTED, store.getRequestStatus());
//	}
//
//	@Test
//	@DisplayName("가게 삭제 요청 실패 테스트 - 이미 삭제 요청됨")
//	void requestDeleteStore_Fail_AlreadyRequested() {
//		ReflectionTestUtils.setField(store, "requestStatus", StoreRequestStatus.DELETE_REQUESTED);
//
//		when(memberRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
//		when(storeRepository.findByIdAndOwnerId(store.getId(), owner.getId())).thenReturn(Optional.of(store));
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.requestDeleteStore(store.getId(), authUser));
//		assertEquals("이미 삭제된 가게입니다.", exception.getMessage());
//	}
//
//	@Test
//	@DisplayName("가게 소유자 가게 조회 성공 테스트")
//	void getStoreById_Success() {
//		ReflectionTestUtils.setField(store, "requestStatus", StoreRequestStatus.APPROVED);
//		when(storeRepository.findByIdAndOwnerId(store.getId(), owner.getId())).thenReturn(Optional.of(store));
//
//		StoreOwnerResponse response = storeService.getStoreById(store.getId(), owner.getId());
//
//		System.out.println("Response Address: " + response.getAddress());
//		System.out.println("Response Description: " + response.getDescription());
//
//		assertNotNull(response);
//		assertEquals("Test Store", response.getName());
//		assertEquals("Test Address", response.getAddress());
//		assertEquals("Test Description", response.getDescription());
//		assertEquals("Test Image", response.getImage());
//	}
//
//	@Test
//	@DisplayName("가게 소유자 가게 조회 실패 테스트 - 가게 없음")
//	void getStoreById_Fail_StoreNotFound() {
//
//		when(storeRepository.findByIdAndOwnerId(store.getId(), owner.getId())).thenReturn(Optional.empty());
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.getStoreById(store.getId(), owner.getId()));
//		assertEquals("해당 가게가 없습니다", exception.getMessage());
//	}
//
//	@Test
//	@DisplayName("가게 소유자 가게 조회 실패 테스트 - 승인 대기 상태")
//	void getStoreById_Fail_ApprovalPending() {
//
//		ReflectionTestUtils.setField(store, "requestStatus", StoreRequestStatus.PENDING);
//		when(storeRepository.findByIdAndOwnerId(store.getId(), owner.getId())).thenReturn(Optional.of(store));
//
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> storeService.getStoreById(store.getId(), owner.getId()));
//		assertEquals("승인 대기중입니다", exception.getMessage());
//	}
//
//}
