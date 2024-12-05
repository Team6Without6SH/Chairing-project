package com.sparta.chairingproject.domain.order.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final MenuRepository menuRepository;

	@Transactional
	public OrderResponse createOrder(Long storeId, UserDetailsImpl authMember, OrderRequest orderRequest) {
		List<Menu> menus = menuRepository.findAllByStoreIdAndMenuIds(storeId, orderRequest.getMenuIds());
		if (menus.size() != orderRequest.getMenuIds().size()) {
			throw new GlobalException(NOT_ORDER_THIS_STORE);
		}

		int totalPrice = menus.stream().mapToInt(Menu::getPrice).sum();
		if (totalPrice != orderRequest.getTotalPrice()) {
			throw new IllegalArgumentException("결제하는 총 가격이 일치하지 않습니다.");
		}

		Order order = Order.createOf(
			authMember.getMember(),
			menus,
			OrderStatus.WAITING,
			totalPrice
		);
		orderRepository.save(order);

		List<String> menuNames = menus.stream()
			.map(Menu::getName)
			.toList();
		return OrderResponse.builder()
			.orderId(order.getId())
			.orderStatus(order.getStatus().name())
			.menuNames(menuNames)
			.totalPrice(order.getPrice())
			.build();
	}
}
