package com.sparta.chairingproject.domain.order.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelRequest extends RequestDto {
	public OrderCancelRequest(Long orderId) {
		super(orderId);
	}
}
