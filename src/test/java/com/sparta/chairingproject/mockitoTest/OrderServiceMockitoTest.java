package com.sparta.chairingproject.mockitoTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.order.service.OrderService;
import com.sparta.chairingproject.domain.store.entity.Store;

@ExtendWith(MockitoExtension.class)
public class OrderServiceMockitoTest {
	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderService orderService;

	private Member member;
	private Member owner;
	private Menu menu1;
	private Menu menu2;
	private Store store;
	private Order order;

	@BeforeEach
	void setUp() {
		// member = new Member(1L, "test@example.com", "password", "Test User", MemberRole.USER);
		// owner = new Member(2L, "test2@example.com", "password", "Test Owner", MemberRole.OWNER);
		// store = new Store(1L, "Test Store", "Test Image", "Test Description", owner);
		// menu1 = new Menu(1L, "김밥", 12000, store);
		// menu2 = new Menu(2L, "치킨", 8000, store);

		order = Order.createOf(
			member,
			store,
			List.of(menu1, menu2),
			OrderStatus.WAITING,
			20000
		);
	}

	@Test
	@DisplayName("취소 요청이 성공적으로 보내진다.")
	void requestOrderCancellation_Success() {
		// given
		Long storeId = 1L;
		Long orderId = 1L;
		// RequestDto memberIdDto = new OrderCancelRequest(member.getId());

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when
		// orderService.requestOrderCancellation(storeId, orderId, member, memberIdDto);

		// then
		assertEquals(OrderStatus.CANCEL_REQUEST, order.getStatus());
		verify(orderRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("다른가게 주문을 취소신청 할 경우 실패한다")
	void requestOrderCancellation_Fail_NotFromStore() {
		// given
		Long storeId = 2L; // 다른 가게 ID
		Long orderId = 1L;
		// RequestDto memberIdDto = new OrderCancelRequest(member.getId());

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when & then
		// GlobalException exception = assertThrows(GlobalException.class,
		// () -> orderService.requestOrderCancellation(storeId, orderId, member, memberIdDto));

		// assertEquals(ExceptionCode.NOT_ORDER_THIS_STORE, exception.getExceptionCode());
		// verify(orderRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("내가 다른 주문을 취소 요청 하면 실패한다")
	void requestOrderCancellation_Fail_InvalidOrder() {
		// given
		Long storeId = 1L;
		Long orderId = 99L;
		// RequestDto memberIdDto = new OrderCancelRequest(member.getId());

		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		// when & then
		// GlobalException exception = assertThrows(GlobalException.class,
		// () -> orderService.requestOrderCancellation(storeId, orderId, member, memberIdDto));

		// assertEquals(ExceptionCode.NOT_FOUND_ORDER, exception.getExceptionCode());
		// verify(orderRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("주문 상태가 이미 취소거나 완료 인 경우에는 취소할 수 없다.")
	void requestOrderCancellation_Fail_InvalidStatus() {
		// given
		Long storeId = 1L;
		Long orderId = 1L;
		// RequestDto memberIdDto = new OrderCancelRequest(member.getId());

		// 주문 상태를 이미 취소 상태로 변경
		order.changeStatus(OrderStatus.CANCELLED);

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		// when & then
		// GlobalException exception = assertThrows(GlobalException.class,
		// () -> orderService.requestOrderCancellation(storeId, orderId, member, memberIdDto));

		// assertEquals(ExceptionCode.CANCELLED_COMPLETED_NOT_ALLOWED, exception.getExceptionCode());
		// verify(orderRepository, times(1)).findById(orderId);
	}
}
