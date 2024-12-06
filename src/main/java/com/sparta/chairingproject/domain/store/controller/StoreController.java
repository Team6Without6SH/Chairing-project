package com.sparta.chairingproject.domain.store.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.order.dto.response.OrderPageResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.service.OrderService;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.service.StoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreController {

	private final StoreService storeService;
	private final OrderService orderService;

	@Secured("ROLE_ADMIN")
	@PostMapping("/owners/stores/register")
	public ResponseEntity<Void> registerStore(
		@Valid @RequestBody StoreRequest storeRequest,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		storeService.registerStore(storeRequest, authMember);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/owners/stores")
	public ResponseEntity<List<StoreResponse>> getAllOpenedStores() {
		List<StoreResponse> stores = storeService.getAllOpenedStores();
		return ResponseEntity.ok(stores);
	}

	@Secured("ROLE_OWNER")
	@GetMapping("/owners/stores/{storeId}/orders")
	public ResponseEntity<Page<OrderPageResponse>> getOrdersByStore(
		@PathVariable Long storeId,
		@PageableDefault(page = 1, size = 5, sort = "createdAt,desc") Pageable pageable,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
		@RequestParam(required = false, defaultValue = "2") int days
	) {
		return ResponseEntity.ok(orderService.getOrdersByStore(storeId, pageable, startDate, endDate, days));
	}
}
