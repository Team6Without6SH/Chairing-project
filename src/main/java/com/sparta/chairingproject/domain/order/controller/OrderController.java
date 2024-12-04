package com.sparta.chairingproject.domain.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.service.OrderService;
import com.sparta.chairingproject.util.ResponseBodyDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	@PostMapping("/{storeId}/orders")
	public ResponseEntity<ResponseBodyDto<OrderResponse>> createOrder(
		@PathVariable Long storeId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@RequestBody OrderRequest orderRequest
	) {
		return new ResponseEntity<>(
			ResponseBodyDto.success("주문 완료", orderService.createOrder(storeId, authMember, orderRequest)),
			HttpStatus.OK);
	}
}
