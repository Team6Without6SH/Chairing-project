package com.sparta.chairingproject.domain.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderPageResponse {
	private Long orderId;
	private Long memberId;
	private LocalDateTime createdAt;
	private List<String> menuNames;
	private String orderStatus;

	public static OrderPageResponse from(Order order) {
		List<String> menuNames = order.getMenus()
			.stream()
			.map(Menu::getName)
			.toList();

		return OrderPageResponse.builder()
			.orderId(order.getId())
			.memberId(order.getMember().getId())
			.createdAt(order.getCreatedAt())
			.menuNames(menuNames)
			.orderStatus(order.getStatus().getDescription())
			.build();
	}
}
