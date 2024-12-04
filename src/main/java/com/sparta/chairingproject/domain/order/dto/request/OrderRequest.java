package com.sparta.chairingproject.domain.order.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequest {
	@NotNull
	private List<Long> menuIds;
	@NotNull
	@Min(value = 10)
	private int totalPrice;
}
