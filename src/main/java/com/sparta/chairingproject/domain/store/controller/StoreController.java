package com.sparta.chairingproject.domain.store.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.order.dto.response.OrderPageResponse;
import com.sparta.chairingproject.domain.order.service.OrderService;
import com.sparta.chairingproject.domain.store.dto.StoreDetailsResponse;
import com.sparta.chairingproject.domain.store.dto.StoreOpenCloseRequest;
import com.sparta.chairingproject.domain.store.dto.StoreOpenCloseResponse;
import com.sparta.chairingproject.domain.store.dto.StoreOwnerResponse;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreRequest;
import com.sparta.chairingproject.domain.store.service.StoreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class StoreController {

	private final StoreService storeService;
	private final OrderService orderService;

	@Secured("ROLE_OWNER")
	@PostMapping("/owners/stores/register")
	public ResponseEntity<StoreResponse> registerStore(
		@Valid @RequestPart(value = "storeRequest") StoreRequest storeRequest,
		@RequestPart(value = "storeImage", required = false) MultipartFile file,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		StoreResponse response = storeService.registerStore(storeRequest, authMember, file);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/members/stores")
	public ResponseEntity<List<StoreResponse>> getAllOpenedStores() {
		List<StoreResponse> stores = storeService.getAllOpenedStores();
		return ResponseEntity.ok(stores);
	}

	@Secured("ROLE_OWNER")
	@GetMapping("/owners/stores/{storeId}/orders")
	public ResponseEntity<Page<OrderPageResponse>> getOrdersByStore(
		@PathVariable Long storeId,
		@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
		@RequestParam(required = false, defaultValue = "2") int days
	) {
		return ResponseEntity.ok(
			orderService.getOrdersByStore(storeId, pageable, startDate, endDate, days));
	}

	@Secured("ROLE_OWNER")
	@PutMapping("/owners/stores/{storeId}")
	public ResponseEntity<StoreDetailsResponse> updateStore(
		@PathVariable Long storeId,
		@Valid @RequestPart(value = "req") UpdateStoreRequest req,
		@RequestPart(value = "storeImage", required = false) MultipartFile file,
		@AuthenticationPrincipal UserDetailsImpl authUser
	) {
		return ResponseEntity.ok(storeService.updateStore(storeId, req, authUser, file));
	}

	@Secured("ROLE_USER")
	@GetMapping("/stores/{storeId}")
	public ResponseEntity<StoreDetailsResponse> getStoreDetails(
		@PathVariable("storeId") Long storeId) {
		StoreDetailsResponse response = storeService.getStoreDetails(storeId);
		return ResponseEntity.ok(response);
	}

	@Secured("ROLE_OWNER")
	@GetMapping("/owners/stores/{storeId}")
	public ResponseEntity<StoreOwnerResponse> getStore(
		@PathVariable Long storeId,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		Long ownerId = authMember.getMember().getId(); // 현재 로그인된 가게 사장 ID
		return ResponseEntity.ok(storeService.getStoreById(storeId, ownerId));
	}

	@Secured("ROLE_OWNER")
	@PostMapping("/owners/stores/{storeId}/delete-request")
	public ResponseEntity<Void> requestDeleteStore(
		@PathVariable Long storeId,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		storeService.requestDeleteStore(storeId, authMember);
		return ResponseEntity.ok().build();
	}

	@Secured("ROLE_OWNER")
	@PatchMapping("/owners/stores/{storeId}")
	public ResponseEntity<StoreOpenCloseResponse> storeOpenClose(
		@PathVariable Long storeId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@RequestBody @Valid StoreOpenCloseRequest request
	) {
		return ResponseEntity.ok(storeService.storeOpenClose(storeId, authMember.getMember(), request));
	}
}