//package com.sparta.chairingproject.domain.menu.service;
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
//import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
//import com.sparta.chairingproject.domain.common.dto.RequestDto;
//import com.sparta.chairingproject.domain.member.entity.Member;
//import com.sparta.chairingproject.domain.member.entity.MemberRole;
//import com.sparta.chairingproject.domain.menu.dto.request.MenuRequest;
//import com.sparta.chairingproject.domain.menu.dto.request.MenuUpdateRequest;
//import com.sparta.chairingproject.domain.menu.dto.response.MenuDeleteResponse;
//import com.sparta.chairingproject.domain.menu.dto.response.MenuDetailResponse;
//import com.sparta.chairingproject.domain.menu.dto.response.MenuResponse;
//import com.sparta.chairingproject.domain.menu.dto.response.MenuUpdateResponse;
//import com.sparta.chairingproject.domain.menu.entity.Menu;
//import com.sparta.chairingproject.domain.menu.entity.MenuStatus;
//import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
//import com.sparta.chairingproject.domain.order.entity.Order;
//import com.sparta.chairingproject.domain.order.entity.OrderStatus;
//import com.sparta.chairingproject.domain.order.repository.OrderRepository;
//import com.sparta.chairingproject.domain.store.entity.Store;
//import com.sparta.chairingproject.domain.store.repository.StoreRepository;
//
//class MenuServiceTest {
//	@InjectMocks
//	private MenuService menuService;
//
//	@Mock
//	private MenuRepository menuRepository;
//
//	@Mock
//	private StoreRepository storeRepository;
//
//	@Mock
//	private OrderRepository orderRepository;
//
//	private Member member;
//	private Member owner;
//	private Member owner2;
//	private Store store;
//	private Order order;
//	private Menu menu;
//	private Menu menu2;
//	private List<Menu> menuList;
//
//	@BeforeEach
//	void setUp() {
//		MockitoAnnotations.openMocks(this);
//		owner = new Member("Test owner", "Test@email.com", "password123", "image",MemberRole.OWNER);
//		ReflectionTestUtils.setField(owner, "id", 1L);
//		owner2 = new Member("Test owner3", "Test@email3.com", "password123","image", MemberRole.OWNER);
//		ReflectionTestUtils.setField(owner2, "id", 3L);
//		member = new Member("Test member", "Test@email2.com", "password123", "image",MemberRole.USER);
//		ReflectionTestUtils.setField(member, "id", 2L);
//		store = new Store("Test Store", "Test Image", "description", "seoul", owner);
//		ReflectionTestUtils.setField(store, "id", 1L);
//		menu = Menu.createOf("menuTest", 5000, "menuImage", store);
//		ReflectionTestUtils.setField(menu, "id", 1L);
//		menu2 = Menu.createOf("menuTest2", 10000, "menuImage", store);
//		ReflectionTestUtils.setField(menu2, "id", 2L);
//		menuList = List.of(menu);
//		order = Order.createOf(member, store, menuList, OrderStatus.WAITING, 5000);
//		ReflectionTestUtils.setField(order, "id", 1L);
//	}
//
//	@Test
//	@DisplayName("정상적으로 메뉴를 생성한다")
//	void createMenu_Success() {
//		// G
//		MenuRequest request = new MenuRequest("새 메뉴", 10000, "image.jpg");
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.existsByStoreAndName(store, request.getName())).thenReturn(false);
//
//		// W
//		MenuResponse response = menuService.createMenu(store.getId(), request, owner);
//
//		// T
//		assertEquals("새 메뉴", response.getName());
//		assertEquals(10000, response.getPrice());
//		verify(menuRepository).save(any(Menu.class));
//	}
//
//	@Test
//	@DisplayName("가게를 찾을 수 없을 경우 예외가 발생: NOT_FOUND_MENU")
//	void createMenu_ThrowException_When_NOT_FOUND_MENU() {
//		// G
//		MenuRequest request = new MenuRequest("새 메뉴", 10000, "image.jpg");
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.createMenu(store.getId(), request, owner));
//		assertEquals(ExceptionCode.NOT_FOUND_MENU, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("요청한 사용자가 가게의 주인이 아닌 경우 예외가 발생: ONLY_OWNER_ALLOWED")
//	void createMenu_ThrowException_When_ONLY_OWNER_ALLOWED() {
//		// G
//		MenuRequest request = new MenuRequest("새 메뉴", 10000, "image.jpg");
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.createMenu(store.getId(), request, owner2));
//		assertEquals(ExceptionCode.ONLY_OWNER_ALLOWED, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("중복된 메뉴 이름으로 요청할 경우 예외 발생: DUPLICATED_MENU")
//	void createMenu_ThrowException_When_DUPLICATED_MENU() {
//		// G
//		MenuRequest request = new MenuRequest("있는 메뉴", 10000, "image.jpg");
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.existsByStoreAndName(store, request.getName())).thenReturn(true);
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.createMenu(store.getId(), request, owner));
//		assertEquals(ExceptionCode.DUPLICATED_MENU, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("정상적으로 메뉴가 수정된다")
//	void updateMenu_Success() {
//		// G
//		MenuUpdateRequest request = new MenuUpdateRequest("수정된 메뉴", 11000, MenuStatus.ACTIVE);
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.findByIdAndStore(menu.getId(), store)).thenReturn(Optional.of(menu));
//
//		// W
//		MenuUpdateResponse response = menuService.updateMenu(store.getId(), menu.getId(), request, owner);
//
//		// T
//		assertEquals("수정된 메뉴", response.getName());
//		assertEquals(11000, response.getPrice());
//		assertEquals(response.getStatus(), MenuStatus.ACTIVE);
//		verify(menuRepository).findByIdAndStore(menu.getId(), store);
//	}
//
//	@Test
//	@DisplayName("가게를 찾을 수 없을 경우 예외 발생: NOT_FOUND_STORE")
//	void updateMenu_ThrowException_When_NOT_FOUND_STORE() {
//		// G
//		MenuUpdateRequest request = new MenuUpdateRequest("수정된 메뉴", 11000, MenuStatus.ACTIVE);
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.updateMenu(store.getId(), menu.getId(), request, owner));
//		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("요청한 사용자가 가게의 주인이 아닌 경우 예외 발생: ONLY_OWNER_ALLOWED")
//	void updateMenu_ThrowException_When_ONLY_OWNER_ALLOWED() {
//		// G
//		MenuUpdateRequest request = new MenuUpdateRequest("수정된 메뉴", 11000, MenuStatus.ACTIVE);
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.updateMenu(store.getId(), menu.getId(), request, owner2));
//		assertEquals(ExceptionCode.ONLY_OWNER_ALLOWED, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("메뉴를 찾을 수 없을 경우 예외 발생: NOT_FOUND_MENU")
//	void updateMenu_ThrowException_When_NOT_FOUND_MENU() {
//		// G
//		MenuUpdateRequest request = new MenuUpdateRequest("수정된 메뉴", 11000, MenuStatus.ACTIVE);
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.findByIdAndStore(menu.getId(), store)).thenReturn(Optional.empty());
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.updateMenu(store.getId(), menu.getId(), request, owner));
//		assertEquals(ExceptionCode.NOT_FOUND_MENU, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("메뉴 정보가 부분적으로 업데이트 되는 경우: 일부만 제공 헀을 때, 그 부분만 업데이트 된다.")
//	void updateMenu_Partial_Update() {
//		// G
//		MenuUpdateRequest request = new MenuUpdateRequest(null, 11000, MenuStatus.ACTIVE);
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.findByIdAndStore(menu.getId(), store)).thenReturn(Optional.of(menu));
//
//		// W
//		MenuUpdateResponse response = menuService.updateMenu(store.getId(), menu.getId(), request, owner);
//
//		// T
//		assertEquals("menuTest", response.getName());
//		assertEquals(11000, response.getPrice());
//		assertEquals(response.getStatus(), MenuStatus.ACTIVE);
//	}
//
//	@Test
//	@DisplayName("가게를 찾을 수 없는 경우 예외 발생: NOT_FOUND_STORE")
//	void deleteMenu_ThrowException_When_NOT_FOUND_STORE() {
//		// G
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.deleteMenu(store.getId(), menu.getId(), owner, new RequestDto()));
//		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("사용자가 가게 주인이 아닌 경우 예외 발생: ONLY_OWNER_ALLOWED")
//	void deleteMenu_ThrowException_When_ONLY_OWNER_ALLOWED() {
//		// G
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.deleteMenu(store.getId(), menu.getId(), owner2, new RequestDto()));
//		assertEquals(ExceptionCode.ONLY_OWNER_ALLOWED, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("정상적으로 메뉴 목록을 반환한다")
//	void getAllMenusByStore_Success() {
//		// G
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.findAllByStoreId(store.getId())).thenReturn(List.of(menu, menu2));
//
//		// W
//		List<MenuDetailResponse> response = menuService.getAllMenusByStore(store.getId(), owner);
//
//		// T
//		assertEquals(2, response.size());
//		assertEquals("menuTest", response.get(0).getName());
//		assertEquals("menuTest2", response.get(1).getName());
//		verify(storeRepository).findById(store.getId());
//		verify(menuRepository).findAllByStoreId(store.getId());
//	}
//
//	@Test
//	@DisplayName("가게를 찾을수 없을 경우 예외가 발생: NOT_FOUND_STORE")
//	void getAllMenusByStore_ThrowException_When_NOT_FOUND_STORE() {
//		// G
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.empty());
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.getAllMenusByStore(store.getId(), owner));
//		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("요청한 사용자가 가게 주인이 아닌 경우 예외가 발생한다: ONLY_OWNER_ALLOWED")
//	void getAllMenuByStore_ThrowException_When_ONLY_OWNER_ALLOWED() {
//		// G
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//
//		// W T
//		GlobalException exception = assertThrows(GlobalException.class,
//			() -> menuService.getAllMenusByStore(store.getId(), owner2));
//		assertEquals(ExceptionCode.ONLY_OWNER_ALLOWED, exception.getExceptionCode());
//	}
//
//	@Test
//	@DisplayName("가게에 메뉴가 없는 경우 빈 목록을 반환한다")
//	void getAllMenusByStore_ThrowException_When_NO_LIST() {
//		// G
//		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
//		when(menuRepository.findAllByStoreId(store.getId())).thenReturn(List.of());
//
//		// W
//		List<MenuDetailResponse> response = menuService.getAllMenusByStore(store.getId(), owner);
//
//		// T
//		assertEquals(0, response.size());
//		verify(storeRepository).findById(store.getId());
//		verify(menuRepository).findAllByStoreId(store.getId());
//	}
//}
