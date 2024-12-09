package com.sparta.chairingproject.domain.order.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {
	private Long orderId;
	private String orderStatus;
	private int totalPrice;
	private List<String> menuNames;
	private int waitingTeams;
}
