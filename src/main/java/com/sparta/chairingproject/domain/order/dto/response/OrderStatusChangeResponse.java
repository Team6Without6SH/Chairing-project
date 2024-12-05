package com.sparta.chairingproject.domain.order.dto.response;

import com.sparta.chairingproject.domain.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusChangeResponse {
	private Long orderId;
	private String orderStatus;
}
