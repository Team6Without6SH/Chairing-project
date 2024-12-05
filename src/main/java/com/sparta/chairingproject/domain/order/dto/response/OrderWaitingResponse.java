package com.sparta.chairingproject.domain.order.dto.response;

import com.sparta.chairingproject.domain.order.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderWaitingResponse {
	private Long orderId;
	private OrderStatus orderStatus;
	private int waitingTeams;
}
