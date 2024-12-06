package com.sparta.chairingproject.domain.order.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusUpdateResponse {
	private Long orderId;
	private String orderStatus;
}
