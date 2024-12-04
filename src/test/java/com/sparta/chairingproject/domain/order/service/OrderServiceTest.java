package com.sparta.chairingproject.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.store.entity.Store;

@SpringBootTest
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
class OrderServiceTest {
	@Mock
	private OrderRepository orderRepository;

	@Mock
	private MenuRepository menuRepository;

	@Mock
	private UserDetailsService userDetailsService;

	@InjectMocks
	private OrderService orderService;

	private Member member;
	private Member owner;
	private Menu menu1;
	private Menu menu2;

	// @BeforeEach
	// void setUp() throws Exception {
	// 	member = new Member(1L, "Test User","test@example.com", "password", MemberRole.USER);
	//
	//
	// 	owner = new Member(2L, "Test User2","test2@example.com", "password", MemberRole.ADMIN);
	// 	menu1 = new Menu(1L, "Pizza", 12000, new Store(1L,"Test Store","Test Image","description", owner));
	// 	menu2 = new Menu(2L, "Burger", 8000, new Store(1L,"Test Store","Test Image","description", owner));
	// }

	@Test
	void testCreateOrder() {
		// given
		List<Long> menuIds = Arrays.asList(menu1.getId(), menu2.getId());
		int totalPrice = 20000;
		UserDetailsImpl authMember = new UserDetailsImpl(member);

		// 메뉴 조회 Mocking
		when(menuRepository.findAllByStoreIdAndMenuIds(anyLong(), eq(menuIds)))
			.thenReturn(Arrays.asList(menu1, menu2));

		// 주문 생성 Mocking
		Order savedOrder = Order.createOf(member, Arrays.asList(menu1, menu2), OrderStatus.WAITING, totalPrice);
		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

		// when
		OrderRequest requestDto = new OrderRequest(menuIds, totalPrice);
		OrderResponse response = orderService.createOrder(1L, authMember, requestDto);

		// then
		assertNotNull(response);
		assertEquals("WAITING", response.getOrderStatus());
		assertEquals(totalPrice, response.getTotalPrice());
		assertEquals(2, response.getMenuNames().size());
		assertTrue(response.getMenuNames().contains("Pizza"));
		assertTrue(response.getMenuNames().contains("Burger"));

		// 메뉴 조회가 호출되었는지 확인
		verify(menuRepository, times(1)).findAllByStoreIdAndMenuIds(anyLong(), eq(menuIds));

		// 주문 저장이 호출되었는지 확인
		verify(orderRepository, times(1)).save(any(Order.class));
	}
}