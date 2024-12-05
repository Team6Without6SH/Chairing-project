package com.sparta.chairingproject.domain.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.order.dto.request.OrderCancelRequest;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.request.OrderStatusChangeRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderCancelResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderStatusChangeResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderWaitingResponse;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;

	@PostMapping("/{storeId}/orders")
	public ResponseEntity<OrderResponse> createOrder(
		@PathVariable Long storeId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@Valid @RequestBody OrderRequest orderRequest
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(orderService.createOrder(storeId, authMember, orderRequest));
	}

	@Secured("ROLE_USER")
	@PutMapping("/{storeId}/orders/{orderId}")
	public ResponseEntity<OrderCancelResponse> requestOrderCancellation(
		@PathVariable Long storeId,
		@PathVariable Long orderId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@RequestBody OrderCancelRequest memberId
	) {
		return ResponseEntity.ok(
			orderService.requestOrderCancellation(storeId, orderId, authMember.getMember(), memberId));
	}

	@Secured("ROLE_USER")
	@PostMapping("/{storeId}/orders/waiting")
	public ResponseEntity<OrderWaitingResponse> createWaiting(
		@PathVariable Long storeId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@RequestBody RequestDto request
	) {
		return ResponseEntity.ok(orderService.createWaiting(storeId, authMember.getMember(), request));
	}

	@Secured("ROLE_OWNER")
	@PatchMapping("/{storeId}/orders/{orderId}/status")
	public ResponseEntity<OrderStatusChangeResponse> updateOrderStatus(
		@PathVariable Long storeId,
		@PathVariable Long orderId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@RequestBody OrderStatusChangeRequest newStatus
	) {
		return ResponseEntity.ok(
			orderService.updateOrderStatus(storeId, orderId, newStatus, authMember.getMember()));
	}
}
