package com.sparta.chairingproject.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.request.OrderStatusUpdateRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderCancelResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderPageResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderStatusUpdateResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderWaitingResponse;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private OrderService orderService;

	private Member member;
	private Member member2;
	private Member owner;
	private Member owner2;
	private Store store;
	private Store store2;
	private Order order;
	private Order cancelledOrder;
	private Order completedOrder;
	private Order anotherStoreOrder;
	private Order admissionOrder;
	private Menu menu;
	private Menu menu2;
	private List<Menu> menuList;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		owner = new Member("Test owner", "Test@email.com", "password123", MemberRole.OWNER);
		ReflectionTestUtils.setField(owner, "id", 1L);
		owner2 = new Member("Test owner3", "Test@email3.com", "password123", MemberRole.OWNER);
		ReflectionTestUtils.setField(owner2, "id", 3L);
		member = new Member("Test member", "Test@email2.com", "password123", MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", 2L);
		member2 = new Member("Test member4", "Test@email4.com", "password123", MemberRole.USER);
		ReflectionTestUtils.setField(member2, "id", 4L);
		store = new Store("Test Store", "Test Image", "description", "seoul", owner);
		ReflectionTestUtils.setField(store, "id", 1L);
		ReflectionTestUtils.setField(store, "tableCount", 5);
		store2 = new Store("Test Store2", "Test Image2", "description2", "seoul", owner);
		ReflectionTestUtils.setField(store2, "id", 2L);
		ReflectionTestUtils.setField(store2, "tableCount", 5);
		menu = Menu.createOf("menuTest", 5000, "menuImage", store);
		ReflectionTestUtils.setField(menu, "id", 1L);
		menu2 = Menu.createOf("menuTest2", 10000, "menuImage", store);
		ReflectionTestUtils.setField(menu2, "id", 2L);
		menuList = List.of(menu);
		order = Order.createOf(member, store, menuList, OrderStatus.WAITING, 5000);
		ReflectionTestUtils.setField(order, "id", 1L);
		cancelledOrder = Order.createOf(member, store, menuList, OrderStatus.CANCELLED, 5000);
		ReflectionTestUtils.setField(cancelledOrder, "id", 2L);
		completedOrder = Order.createOf(member, store, menuList, OrderStatus.COMPLETED, 5000);
		ReflectionTestUtils.setField(completedOrder, "id", 3L);
		anotherStoreOrder = Order.createOf(member, store2, menuList, OrderStatus.WAITING, 5000);
		ReflectionTestUtils.setField(anotherStoreOrder, "id", 4L);
		admissionOrder = Order.createOf(member, store, menuList, OrderStatus.ADMISSION, 5000);
		ReflectionTestUtils.setField(admissionOrder, "id", 5L);
	}

	// @Test
	// @DisplayName("테이블이 남아 있을때는 ADMISSION 상태를 반환한다.")
	// void createWaiting_ReturnADMISSION_When_Table_Available() {
	// 	//given
	// 	when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store)); //store 반환시키기
	//
	// 	//when
	// 	RequestDto requestDto = new RequestDto();
	// 	OrderWaitingResponse response = orderService.createWaiting(store.getId(), member, requestDto);
	//
	// 	// 결과 검증
	// 	assertNotNull(response);
	// 	assertEquals(OrderStatus.ADMISSION, response.getOrderStatus());
	// 	assertEquals(0, response.getWaitingTeams());
	// }

	// @Test
	// @DisplayName("테이블이 다 차 있으면 WAITING 상태를 반환한다.")
	// void createWaiting_ReturnWAITING_When_Table_Full() {
	// 	// given
	// 	when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
	// 	when(orderRepository.countByStoreIdAndStatus(store.getId(), OrderStatus.IN_PROGRESS)).thenReturn(
	// 		5); //현재 식사중인 테이블을 5로 설정
	//
	// 	// when
	// 	RequestDto requestDto = new RequestDto();
	// 	OrderWaitingResponse response = orderService.createWaiting(store.getId(), member, requestDto);
	//
	// 	// then
	// 	assertNotNull(response);
	// 	assertEquals(OrderStatus.WAITING, response.getOrderStatus());
	// 	assertEquals(0, response.getWaitingTeams());
	// }

	@Test
	@DisplayName("유효하지 않은 storeId로 주문을 시도하면 예외가 발생한다.")
	void createWaiting_ThrowException_When_InvalidStoreId() {
		// given
		Long storeId = 999L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty()); // store가 없을 때

		// when & then
		RequestDto requestDto = new RequestDto();
		assertThrows(GlobalException.class, () -> orderService.createWaiting(storeId, member, requestDto));
	}

	// @Test
	// @DisplayName("메뉴가 선택되고, 웨이팅을 신청할 때, 테이블에 자리가 있으면 ADMISSION 상태로 주문 생성된다.")
	// void createOrder_ReturnAdmission_When_Table_Available() {
	// 	// given
	// 	OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 15000); //메뉴 두개 주문하고 가격을 합에 맞춰 설정하기
	// 	when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
	// 	when(menuRepository.findAllByStoreIdAndMenuIds(store.getId(), List.of(1L, 2L))).thenReturn(
	// 		List.of(menu, menu2));
	// 	when(orderRepository.countByStoreIdAndStatus(store.getId(), OrderStatus.IN_PROGRESS)).thenReturn(
	// 		3); // 진행 중인 주문 수 3개
	//
	// 	// when
	// 	OrderResponse response = orderService.createOrder(store.getId(), member, orderRequest);
	//
	// 	// then
	// 	assertNotNull(response);
	// 	assertEquals(OrderStatus.ADMISSION.name(), response.getOrderStatus()); // 상태는 ADMISSION
	// 	assertEquals(15000, response.getTotalPrice()); // 총 가격 검증
	// 	assertTrue(response.getMenuNames().contains("menuTest"));
	// 	assertTrue(response.getMenuNames().contains("menuTest2"));
	// }

	// @Test
	// @DisplayName("메뉴를 선택하지 않고, 웨이팅을 신청할 때, 테이블에 자리가 없으면 WAITING 상태로 주문이 생성된다.")
	// void createOrder_ReturnWaiting_When_NoMenu_TableFull() {
	// 	//given
	// 	OrderRequest orderRequest = new OrderRequest(Collections.emptyList(), 0);
	// 	when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
	// 	when(orderRepository.countByStoreIdAndStatus(store.getId(), OrderStatus.IN_PROGRESS)).thenReturn(
	// 		5); // 진행 중인 주문 수가 5
	//
	// 	//when
	// 	OrderResponse response = orderService.createOrder(store.getId(), member, orderRequest);
	//
	// 	//then
	// 	assertNotNull(response);
	// 	assertEquals(OrderStatus.WAITING.name(), response.getOrderStatus()); // 상태 WAITING
	// 	assertEquals(0, response.getTotalPrice()); // 금액 0 d인지 확인
	// 	assertTrue(response.getMenuNames().isEmpty()); // 메뉴는 신청 잘 안했는지
	// }

	// @Test
	// @DisplayName("메뉴를 선택하지 않고, 웨이팅을 신청할 때, 테이블에 자리가 없고, WAITING 인 상태가 3팀인 경우 3이 반환이 된다.")
	// void createOrder_ReturnWaiting_WithTheNumberOfTeams_When_NoMenu_TableFull() {
	// 	//given
	// 	OrderRequest orderRequest = new OrderRequest(Collections.emptyList(), 0);
	// 	when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
	// 	when(orderRepository.countByStoreIdAndStatus(store.getId(), OrderStatus.IN_PROGRESS)).thenReturn(5);
	// 	when(orderRepository.countByStoreIdAndStatus(store.getId(), OrderStatus.WAITING)).thenReturn(3);
	//
	// 	// when
	// 	OrderResponse response = orderService.createOrder(store.getId(), member, orderRequest);
	//
	// 	//then
	// 	assertNotNull(response);
	// 	assertEquals(OrderStatus.WAITING.name(), response.getOrderStatus()); // 상태는 WAITING
	// 	assertEquals(0, response.getTotalPrice()); // 총 금액은 0
	// 	assertEquals(3, response.getWaitingTeams()); // 앞에 대기 팀 수 3팀
	// }

	@Test
	@DisplayName("선택된 메뉴 수와, 실제 메뉴 수가 일치하지 않으면 예외가 발생한다.")
	void createOrder_ThrowException_When_TheNumberOfMenuSelected_Not_Equal_To_StoreMenus() {
		// given
		OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 10000); //메뉴 두개 주문
		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
		when(menuRepository.findAllByStoreIdAndMenuIds(store.getId(), List.of(1L, 2L))).thenReturn(
			List.of(menu)); // 메뉴가 1번밖에 없기때문

		//when then
		assertThrows(GlobalException.class,
			() -> orderService.createOrder(store.getId(), member, orderRequest)); // 예외 발생
	}

	@Test
	@DisplayName("주문 금액이 총 금액과 일치하지 않으면 예외가 발생한다.")
	void createOrder_ThrowException_When_TotalPrice_Not_Equal_To_paidPrice() {
		// given
		OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 16000); // 메뉴 가격을 잘못 입력했을 때
		when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
		when(menuRepository.findAllByStoreIdAndMenuIds(store.getId(), List.of(1L, 2L))).thenReturn(
			List.of(menu, menu2));

		// when & then
		assertThrows(GlobalException.class, () -> orderService.createOrder(store.getId(), member, orderRequest));
	}

	@Test
	@DisplayName("존재하지 않는 주문 ID 로 요청 시 예외가 발생한다.")
	void requestOrderCancellation_ThrowException_When_OrderNotFound() {
		when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(store.getId(), order.getId(), member, null));
		assertEquals(ExceptionCode.NOT_FOUND_ORDER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("요청자가 주문자가 아닌 경우 예외가 발생한다.")
	void requestOrderCancellation_ThrowException_When_MemberNotOrder() {
		Long storeId = 1L;
		Long orderId = 1L;

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, member2, null));
		assertEquals(ExceptionCode.ONLY_ORDER_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("해당 가게의 주문이 아닌 경우에 예외가 발생한다.")
	void requestOrderCancellation_ThrowsException_When_NOT_ORDER_THIS_STORE() {
		Long orderId = 1L;
		Long storeRequestId = 2L;
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeRequestId, orderId, member, null));
		assertEquals(ExceptionCode.NOT_ORDER_THIS_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문 상태가 CANCELLED 인 경우에 예외가 발생한다.")
	void requestOrderCancellation_ThrowsException_When_OrderAlreadyCancelled() {
		Long storeId = 1L;
		Long orderId = 2L;
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(cancelledOrder));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, member, null));
		assertEquals(ExceptionCode.CANCELLED_COMPLETED_NOT_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문 상태가 COMPLETED 인 경우에 예외가 발생한다.")
	void requestOrderCancellation_ThrowsException_When_OrderAlreadyCompleted() {
		Long storeId = 1L;
		Long orderId = 3L;
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, member, null));
		assertEquals(ExceptionCode.CANCELLED_COMPLETED_NOT_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("정상적으로 주문 취소 요청이 처리된다.")
	void requestOrderCancellation_Success() {
		Long storeId = 1L;
		Long orderId = 1L;
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		//when
		OrderCancelResponse response = orderService.requestOrderCancellation(storeId, orderId, member, null);

		//then
		assertNotNull(response);
		assertEquals(OrderStatus.CANCEL_REQUEST, response.getOrderStatus());
		assertEquals(orderId, response.getOrderId());
	}

	@Test
	@DisplayName("가게가 존재하지 않을 경우: NOT_FOUND_STORE")
	void updateOrderStatus_ThrowException_When_storeNotFound() {
		Long storeId = 1L;
		Long orderId = 2L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());
		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));
		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("요청자가 가게 소유자가 아닌 경우: ONLY_OWNER_ALLOWED")
	void updateOrderStatus_ThrowException_When_RequesterIsNotOwner() {
		Long storeId = 1L;
		Long orderId = 2L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"),
				owner2));
		assertEquals(ExceptionCode.ONLY_OWNER_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문이 존재하지 않는 경우: NOT_FOUND_ORDER")
	void updateOrderStatus_ThrowException_When_OrderNotFound() {
		Long storeId = 1L;
		Long orderId = 999L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));
		assertEquals(ExceptionCode.NOT_FOUND_ORDER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문이 요청한 가게와 일치하지 않는 경우: NOT_ORDER_THIS_STORE")
	void updateOrderStatus_ThrowsException_When_NotOrderThisStore() {
		Long storeId = 1L;
		Long orderId = 4L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(anotherStoreOrder));

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, anotherStoreOrder.getId(),
				new OrderStatusUpdateRequest("ADMISSION"), owner));
		assertEquals(ExceptionCode.NOT_ORDER_THIS_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("사장님이 CANCEL_REQUEST 상태로 변경하는 경우: CANCEL_REQUEST_NOT_ALLOWED_BY_OWNER")
	void updateOrderStatus_ThrowException_When_CancelRequestNotAllowedByOwner() {
		Long storeId = 1L;
		Long orderId = 1L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("CANCEL_REQUEST"),
				owner));
		assertEquals(ExceptionCode.CANCEL_REQUEST_NOT_ALLOWED_BY_OWNER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문 상태가 COMPLETED 또는 CANCELLED 인 경우 변경 시도: CANNOT_CHANGE_COMPLETE_OR_CANCELLED")
	void updateOrderStatus_ThrowException_When_CannotChangeCompleteOrCancelled() {
		Long storeId = 1L;
		Long orderId = 3L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));

		assertEquals(ExceptionCode.CANNOT_CHANGE_COMPLETED_OR_CANCELLED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("IN_PROGRESS 상태로 변경 할 때, 테이블이 다 차있는 경우: TABLE_FULL_CANNOT_SET_IN_PROGRESS")
	void updateOrderStatus_ThrowException_When_TableFullCanNotSetInProgress() {
		Long storeId = 1L;
		Long orderId = 1L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS)).thenReturn(5);

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));
		assertEquals(ExceptionCode.TABLE_FULL_CANNOT_SET_IN_PROGRESS, exception.getExceptionCode());
	}

	@Test
	@DisplayName("WAITING 상태에서 ADMISSION 과 CANCELLED 이외의 상태로 변경하려는 경우: CANCELLED_ADMISSION_ALLOWED_FROM_WAITING")
	void updateOrderStatus_ThrowException_When_CancelledAdmissionAllowedFromWaiting() {
		Long storeId = 1L;
		Long orderId = 1L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("COMPLETED"), owner));
		assertEquals(ExceptionCode.CANCELLED_ADMISSION_ALLOWED_FROM_WAITING, exception.getExceptionCode());
	}

	@Test
	@DisplayName("ADMISSION 상태에서 IN_PROGRESS 와 CANCELLED 이외의 상태로 변경하려는 경우: IN_PROGRESS_CANCELLED_ALLOWED_FROM_ADMISSION")
	void updateOrderStatus_ThrowException_When_InProgressCancelledAllowedFromAdmission() {
		Long storeId = 1L;
		Long orderId = 1L;
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(admissionOrder));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("COMPLETED"), owner));

		assertEquals(ExceptionCode.IN_PROGRESS_CANCELLED_ALLOWED_FROM_ADMISSION, exception.getExceptionCode());
	}

	// @Test
	// @DisplayName("정상적으로 상태변경에 성공하는 경우: 200. OK")
	// void updateOrderStatus_Success() {
	// 	Long storeId = 1L;
	// 	Long orderId = 1L;
	// 	when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
	// 	when(orderRepository.findById(orderId)).thenReturn(Optional.of(admissionOrder));
	//
	// 	//when
	// 	OrderStatusUpdateResponse response = orderService.updateOrderStatus(storeId, orderId,
	// 		new OrderStatusUpdateRequest("IN_PROGRESS"), owner);
	//
	// 	//then
	// 	assertEquals("IN_PROGRESS", response.getOrderStatus());
	// 	verify(orderRepository).save(any(Order.class));
	// }

	@Test
	@DisplayName("가게가 없을때 예외가 발생한다")
	void getOrdersByStore_ThrowException_When_StoreNotFound() {
		Long storeId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.getOrdersByStore(storeId, pageable, null, null, 30));
		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("기간 입력 없이 호출할 경우 전체 주문 목록을 반환한다")
	void getOrdersByStore_ReturnAllOrders_When_NoDate() {
		Long storeId = 1L;
		Pageable pageable = PageRequest.of(0, 10);
		Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByStoreAndCreatedAtBetween(eq(storeId), any(LocalDateTime.class),
			any(LocalDateTime.class), eq(pageable))).thenReturn(orderPage);

		//when
		Page<OrderPageResponse> result = orderService.getOrdersByStore(storeId, pageable, null, null, 30);

		//then
		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("startDate 없이 endDate 만 입력하면 days 기준으로 앞 기간을 계산하여 조회한다.")
	void getOrdersByStore_StartDateNO_EndDateYES() {
		Long storeId = 1L;
		LocalDate endDate = LocalDate.of(2024, 12, 31);
		Pageable pageable = PageRequest.of(0, 10);
		int days = 30;
		Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByStoreAndCreatedAtBetween(eq(storeId), any(LocalDateTime.class),
			eq(endDate.atTime(23, 59, 59)), eq(pageable))).thenReturn(orderPage);

		// when
		Page<OrderPageResponse> result = orderService.getOrdersByStore(storeId, pageable, null, endDate, days);

		// then
		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("endDate 없이 startDate 만 입력하면 days 기준으로 뒤 기간을 계산하여 조회한다.")
	void getOrdersByStore_StartDateYES_EndDateNO() {
		Long storeId = 1L;
		LocalDate startDate = LocalDate.of(2024, 12, 31);
		Pageable pageable = PageRequest.of(0, 10);
		Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);
		int days = 30;

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByStoreAndCreatedAtBetween(eq(storeId), eq(startDate.atStartOfDay()),
			any(LocalDateTime.class), eq(pageable))).thenReturn(orderPage);

		// when
		Page<OrderPageResponse> result = orderService.getOrdersByStore(storeId, pageable, startDate, null, days);

		// then
		assertEquals(1, result.getTotalElements());
	}

	@Test
	@DisplayName("startDate 와 endDate 가 모두 입력되면 지정된 기간에 해당하는 주문 목록을 반환한다.")
	void getOrdersByStore_StartDateYES_EndDateYES() {
		Long storeId = 1L;
		LocalDate startDate = LocalDate.of(2024, 12, 21);
		LocalDate endDate = LocalDate.of(2024, 12, 31);
		Pageable pageable = PageRequest.of(0, 10);
		int days = 30;
		Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByStoreAndCreatedAtBetween(eq(storeId), eq(startDate.atStartOfDay()),
			eq(endDate.atTime(23, 59, 59)), eq(pageable))).thenReturn(orderPage);

		//when
		Page<OrderPageResponse> result = orderService.getOrdersByStore(storeId, pageable, startDate, endDate, days);

		//then
		assertEquals(1, result.getTotalElements());
	}

}
