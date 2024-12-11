package com.sparta.chairingproject.domain.order.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
	private Long orderId;
	private String orderStatus;
	private int totalPrice;
	private List<String> menuNames;
	private int waitingTeams;
}
