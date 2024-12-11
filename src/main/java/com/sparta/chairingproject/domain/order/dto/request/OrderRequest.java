package com.sparta.chairingproject.domain.order.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequest {
	private List<Long> menuIds;
	@NotNull
	@Min(value = 0)
	private int totalPrice;
}
