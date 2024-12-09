package com.sparta.chairingproject.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderWaitingResponse;
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
			"21:00", "Korean", true);

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
			"09:00", "21:00", "Korean", true); // 테이블 수 5으로 설정

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
			"09:00", "21:00", "Korean", true);

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
}
