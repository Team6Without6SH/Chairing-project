package com.sparta.chairingproject.domain.order.message;

import com.sparta.chairingproject.domain.order.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusMessage {
	private Long orderId;
	private String newStatus;
}
