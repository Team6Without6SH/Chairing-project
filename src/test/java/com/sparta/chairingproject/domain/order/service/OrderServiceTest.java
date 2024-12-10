package com.sparta.chairingproject.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderCancelResponse;
import com.sparta.chairingproject.domain.order.dto.request.OrderStatusUpdateRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderStatusUpdateResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderWaitingResponse;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private StoreRepository storeRepository;

	@InjectMocks
	private OrderService orderService;

	@Test
	@DisplayName("테이블이 남아 있을때는 ADMISSION 상태를 반환한다.")
	public void createWaiting_ReturnADMISSION_When_Table_Available() {
		//given
		Long storeId = 1L;
		Member owner = new Member(1L, "Test owner", "Test@email.com", "password123", MemberRole.OWNER);
		Member member = new Member(2L, "Test member", "Test@email2.com", "password123", MemberRole.USER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00",
			"21:00", "Korean");

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store)); //store 반환시키기

		//when
		RequestDto requestDto = new RequestDto();
		OrderWaitingResponse response = orderService.createWaiting(storeId, member, requestDto);

		// 결과 검증
		assertNotNull(response);
		assertEquals(OrderStatus.ADMISSION, response.getOrderStatus());
		assertEquals(0, response.getWaitingTeams());
	}

	@Test
	@DisplayName("테이블이 다 차 있으면 WAITING 상태를 반환한다.")
	public void createWaiting_ReturnWAITING_When_Table_Full() {
		// given
		Long storeId = 1L;
		Member owner = new Member(1L, "Test owner", "Test@email.com", "password123", MemberRole.OWNER);
		Member member = new Member(2L, "Test member", "Test@email2.com", "password123", MemberRole.USER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean"); // 테이블 수 5으로 설정

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS)).thenReturn(
			5); //현재 식사중인 테이블을 5로 설정

		// when
		RequestDto requestDto = new RequestDto();
		OrderWaitingResponse response = orderService.createWaiting(storeId, member, requestDto);

		// then
		assertNotNull(response);
		assertEquals(OrderStatus.WAITING, response.getOrderStatus());
		assertEquals(0, response.getWaitingTeams());
	}

	@Test
	@DisplayName("유효하지 않은 storeId로 주문을 시도하면 예외가 발생한다.")
	public void createWaiting_ThrowException_When_InvalidStoreId() {
		// given
		Long storeId = 999L;
		Member member = new Member(2L, "Test member", "Test@email2.com", "password123", MemberRole.USER);

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty()); // store가 없을 때

		// when & then
		RequestDto requestDto = new RequestDto();
		assertThrows(GlobalException.class, () -> orderService.createWaiting(storeId, member, requestDto));
	}

	@Test
	@DisplayName("메뉴가 선택되고, 웨이팅을 신청할 때, 테이블에 자리가 있으면 ADMISSION 상태로 주문 생성된다.")
	public void createOrder_ReturnAdmission_When_Table_Available() {
		// given
		Long storeId = 1L;
		Member member = new Member(1L, "Test Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 200); //메뉴 두개 주문하고 가격을 합에 맞춰 설정하기

		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Menu menu1 = new Menu(1L, 90, "Menu1", store);
		Menu menu2 = new Menu(2L, 110, "Menu2", store);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(menuRepository.findAllByStoreIdAndMenuIds(storeId, List.of(1L, 2L))).thenReturn(List.of(menu1, menu2));
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS)).thenReturn(3); // 진행 중인 주문 수 3개

		// when
		OrderResponse response = orderService.createOrder(storeId, member, orderRequest);

		// then
		assertNotNull(response);
		assertEquals(OrderStatus.ADMISSION.name(), response.getOrderStatus()); // 상태는 ADMISSION
		assertEquals(200, response.getTotalPrice()); // 총 가격 검증
		assertTrue(response.getMenuNames().contains("Menu1"));
		assertTrue(response.getMenuNames().contains("Menu2"));
	}

	@Test
	@DisplayName("메뉴를 선택하지 않고, 웨이팅을 신청할 때, 테이블에 자리가 없으면 WAITING 상태로 주문이 생성된다.")
	public void createOrder_ReturnWaiting_When_NoMenu_TableFull() {
		//given
		Long storeId = 1L;
		Member member = new Member(1L, "Test Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		OrderRequest orderRequest = new OrderRequest(Collections.emptyList(), 0);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS)).thenReturn(5); // 진행 중인 주문 수가 5

		//when
		OrderResponse response = orderService.createOrder(storeId, member, orderRequest);

		//then
		assertNotNull(response);
		assertEquals(OrderStatus.WAITING.name(), response.getOrderStatus()); // 상태 WAITING
		assertEquals(0, response.getTotalPrice()); // 금액 0 d인지 확인
		assertTrue(response.getMenuNames().isEmpty()); // 메뉴는 신청 잘 안했는지
	}

	@Test
	@DisplayName("메뉴를 선택하지 않고, 웨이팅을 신청할 때, 테이블에 자리가 없고, WAITING 인 상태가 3팀인 경우 3이 반환이 된다.")
	public void createOrder_ReturnWaiting_WithTheNumberOfTeams_When_NoMenu_TableFull() {
		//given
		Long storeId = 1L;
		Member member = new Member(1L, "Test Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		OrderRequest orderRequest = new OrderRequest(Collections.emptyList(), 0);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS)).thenReturn(5);
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.WAITING)).thenReturn(3);

		// when
		OrderResponse response = orderService.createOrder(storeId, member, orderRequest);

		//then
		assertNotNull(response);
		assertEquals(OrderStatus.WAITING.name(), response.getOrderStatus()); // 상태는 WAITING
		assertEquals(0, response.getTotalPrice()); // 총 금액은 0
		assertEquals(3, response.getWaitingTeams()); // 앞에 대기 팀 수 3팀
	}

	@Test
	@DisplayName("선택된 메뉴 수와, 실제 메뉴 수가 일치하지 않으면 예외가 발생한다.")
	public void createOrder_ThrowException_When_TheNumberOfMenuSelected_Not_Equal_To_StoreMenus() {
		// given
		Long storeId = 1L;
		Member member = new Member(1L, "Test Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 90); //메뉴 두개 주문

		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Menu menu1 = new Menu(1L, 90, "Menu1", store);
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(menuRepository.findAllByStoreIdAndMenuIds(storeId, List.of(1L, 2L))).thenReturn(
			List.of(menu1)); // 메뉴가 1번밖에 없기때문

		//when then
		assertThrows(GlobalException.class, () -> orderService.createOrder(storeId, member, orderRequest)); // 예외 발생
	}

	@Test
	@DisplayName("주문 금액이 총 금액과 일치하지 않으면 예외가 발생한다.")
	public void createOrder_ThrowException_When_TotalPrice_Not_Equal_To_paidPrice() {
		// given
		Long storeId = 1L;
		Member member = new Member(1L, "Test Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		OrderRequest orderRequest = new OrderRequest(List.of(1L, 2L), 210); // 메뉴 가격을 잘못 입력했을 때

		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Menu menu1 = new Menu(1L, 90, "Menu1", store);
		Menu menu2 = new Menu(2L, 110, "Menu2", store);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(menuRepository.findAllByStoreIdAndMenuIds(storeId, List.of(1L, 2L))).thenReturn(List.of(menu1, menu2));

		// when & then
		assertThrows(GlobalException.class, () -> orderService.createOrder(storeId, member, orderRequest));
	}

	@Test
	@DisplayName("존재하지 않는 주문 ID 로 요청 시 예외가 발생한다.")
	public void requestOrderCancellation_ThrowException_When_OrderNotFound() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member member = new Member(1L, "Test Member", "Test@email.com", "password123", MemberRole.USER);

		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, member, null));

		assertEquals(ExceptionCode.NOT_FOUND_ORDER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("요청자가 주문자가 아닌 경우 예외가 발생한다.")
	public void requestOrderCancellation_ThrowException_When_MemberNotOrder() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member orderMember = new Member(1L, "order Member", "Test@email.com", "password123", MemberRole.USER);
		Member anotherMember = new Member(2L, "another Member", "Test2@email.com", "password123", MemberRole.USER);
		Member owner = new Member(3L, "사장 Member", "Test3@email.com", "password123", MemberRole.OWNER);

		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Order order = new Order(orderId, orderMember, store, OrderStatus.IN_PROGRESS, 0);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, anotherMember, null));

		assertEquals(ExceptionCode.ONLY_ORDER_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("해당 가게의 주문이 아닌 경우에 예외가 발생한다.")
	public void requestOrderCancellation_ThrowsException_When_NOT_ORDER_THIS_STORE() {
		Long orderId = 1L;
		Long storeRequestId = 2L;
		Member orderMember = new Member(1L, "order Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(3L, "사장 Member", "Test3@email.com", "password123", MemberRole.OWNER);

		Store store1 = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Order order = new Order(orderId, orderMember, store1, OrderStatus.IN_PROGRESS, 0);
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeRequestId, orderId, orderMember, null));

		assertEquals(ExceptionCode.NOT_ORDER_THIS_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문 상태가 CANCELLED 인 경우에 예외가 발생한다.")
	public void requestOrderCancellation_ThrowsException_When_OrderAlreadyCancelled() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member orderMember = new Member(1L, "order Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(3L, "사장 Member", "Test3@email.com", "password123", MemberRole.OWNER);

		Store store1 = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Order order = new Order(orderId, orderMember, store1, OrderStatus.CANCELLED, 0);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, orderMember, null));

		assertEquals(ExceptionCode.CANCELLED_COMPLETED_NOT_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문 상태가 COMPLETED 인 경우에 예외가 발생한다.")
	public void requestOrderCancellation_ThrowsException_When_OrderAlreadyCompleted() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member orderMember = new Member(1L, "order Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(3L, "사장 Member", "Test3@email.com", "password123", MemberRole.OWNER);

		Store store1 = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Order order = new Order(orderId, orderMember, store1, OrderStatus.COMPLETED, 0);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.requestOrderCancellation(storeId, orderId, orderMember, null));

		assertEquals(ExceptionCode.CANCELLED_COMPLETED_NOT_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("정상적으로 주문 취소 요청이 처리된다.")
	public void requestOrderCancellation_Success() {
		Long storeId = 1L;
		Long orderId = 1L;

		Member orderMember = new Member(1L, "order Member", "Test@email.com", "password123", MemberRole.USER);
		Member owner = new Member(3L, "사장 Member", "Test3@email.com", "password123", MemberRole.OWNER);

		Store store1 = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		Order order = new Order(orderId, orderMember, store1, OrderStatus.IN_PROGRESS, 0);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		//when
		OrderCancelResponse response = orderService.requestOrderCancellation(storeId, orderId, orderMember, null);

		//then
		assertNotNull(response);
		assertEquals(OrderStatus.CANCEL_REQUEST, response.getOrderStatus());
		assertEquals(orderId, response.getOrderId());
	}

	@Test
	@DisplayName("가게가 존재하지 않을 경우: NOT_FOUND_STORE")
	public void updateOrderStatus_ThrowException_When_storeNotFound() {
		Long storeId = 1L;
		Long orderId = 2L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());
		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));

		assertEquals(ExceptionCode.NOT_FOUND_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("요청자가 가게 소유자가 아닌 경우: ONLY_OWNER_ALLOWED")
	public void updateOrderStatus_ThrowException_When_RequesterIsNotOwner() {
		Long storeId = 1L;
		Long orderId = 2L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Member owner2 = new Member(3L, "Test owner2", "Test3@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"),
				owner2));

		assertEquals(ExceptionCode.ONLY_OWNER_ALLOWED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문이 존재하지 않는 경우: NOT_FOUND_ORDER")
	public void updateOrderStatus_ThrowException_When_OrderNotFound() {
		Long storeId = 1L;
		Long orderId = 2L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));

		assertEquals(ExceptionCode.NOT_FOUND_ORDER, exception.getExceptionCode());
	}

	@Test
	@DisplayName("주문이 요청한 가게와 일치하지 않는 경우: NOT_ORDER_THIS_STORE")
	public void updateOrderStatus_ThrowsException_When_NotOrderThisStore() {
		Long storeId = 1L;
		Long orderId = 2L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Store differentStore = new Store(2L, "Test Store2", "Test Image", "description", owner, 5, "seoul",
			"010-1111-2223",
			"09:00", "21:00", "Korean");
		Order order = Order.createOf(owner, differentStore, Collections.emptyList(), OrderStatus.WAITING, 0);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));

		assertEquals(ExceptionCode.NOT_ORDER_THIS_STORE, exception.getExceptionCode());
	}

	@Test
	@DisplayName("사장님이 CANCEL_REQUEST 상태로 변경하는 경우: CANCEL_REQUEST_NOT_ALLOWED_BY_OWNER")
	public void updateOrderStatus_ThrowException_When_CancelRequestNotAllowedByOwner() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Order order = Order.createOf(owner, store, Collections.emptyList(), OrderStatus.IN_PROGRESS, 0);

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
	public void updateOrderStatus_ThrowException_When_CannotChangeCompleteOrCancelled() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Order completedOrder = Order.createOf(owner, store, Collections.emptyList(), OrderStatus.COMPLETED, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(completedOrder));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));

		assertEquals(ExceptionCode.CANNOT_CHANGE_COMPLETED_OR_CANCELLED, exception.getExceptionCode());
	}

	@Test
	@DisplayName("IN_PROGRESS 상태로 변경 할 때, 테이블이 다 차있는 경우: TABLE_FULL_CANNOT_SET_IN_PROGRESS")
	public void updateOrderStatus_ThrowException_When_TableFullCanNotSetInProgress() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Order waitingOrder = Order.createOf(owner, store, Collections.emptyList(), OrderStatus.ADMISSION, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(waitingOrder));
		when(orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS)).thenReturn(5);

		//when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("IN_PROGRESS"), owner));

		assertEquals(ExceptionCode.TABLE_FULL_CANNOT_SET_IN_PROGRESS, exception.getExceptionCode());
	}

	@Test
	@DisplayName("WAITING 상태에서 ADMISSION 과 CANCELLED 이외의 상태로 변경하려는 경우: CANCELLED_ADMISSION_ALLOWED_FROM_WAITING")
	public void updateOrderStatus_ThrowException_When_CancelledAdmissionAllowedFromWaiting() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Order waitingOrder = Order.createOf(owner, store, Collections.emptyList(), OrderStatus.WAITING, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(waitingOrder));

		// when then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("COMPLETED"), owner));

		assertEquals(ExceptionCode.CANCELLED_ADMISSION_ALLOWED_FROM_WAITING, exception.getExceptionCode());
	}

	@Test
	@DisplayName("ADMISSION 상태에서 IN_PROGRESS 와 CANCELLED 이외의 상태로 변경하려는 경우: IN_PROGRESS_CANCELLED_ALLOWED_FROM_ADMISSION")
	public void updateOrderStatus_ThrowException_When_InProgressCancelledAllowedFromAdmission() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Order admissionOrder = Order.createOf(owner, store, Collections.emptyList(), OrderStatus.ADMISSION, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(admissionOrder));

		GlobalException exception = assertThrows(GlobalException.class,
			() -> orderService.updateOrderStatus(storeId, orderId, new OrderStatusUpdateRequest("COMPLETED"), owner));

		assertEquals(ExceptionCode.IN_PROGRESS_CANCELLED_ALLOWED_FROM_ADMISSION, exception.getExceptionCode());
	}

	@Test
	@DisplayName("정상적으로 상태변경에 성공하는 경우: 200. OK")
	public void updateOrderStatus_Success() {
		Long storeId = 1L;
		Long orderId = 1L;
		Member owner = new Member(2L, "Test owner", "Test2@email.com", "password123", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "Test Image", "description", owner, 5, "seoul", "010-1111-2222",
			"09:00", "21:00", "Korean");
		Order admissionOrder = Order.createOf(owner, store, Collections.emptyList(), OrderStatus.ADMISSION, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findById(orderId)).thenReturn(Optional.of(admissionOrder));

		//when
		OrderStatusUpdateResponse response = orderService.updateOrderStatus(storeId, orderId,
			new OrderStatusUpdateRequest("IN_PROGRESS"), owner);

		//then
		assertEquals("IN_PROGRESS", response.getOrderStatus());
		verify(orderRepository).save(any(Order.class));
	}
}
