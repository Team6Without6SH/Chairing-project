package com.sparta.chairingproject.domain.store.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.domain.store.dto.StoreAdminResponse;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreStatusRequest;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.service.AdminStoreService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {

	private final AdminStoreService adminStoreService;

	@Secured("ROLE_ADMIN")
	@GetMapping
	public ResponseEntity<List<StoreAdminResponse>> getAllStores() {
		List<StoreAdminResponse> stores = adminStoreService.getAllStores();
		return ResponseEntity.ok(stores);
	}

	// 단일 가게 조회
	@Secured("ROLE_ADMIN")
	@GetMapping("/{storeId}")
	public ResponseEntity<StoreAdminResponse> getStoreById(@PathVariable Long storeId) {
		return ResponseEntity.ok(adminStoreService.getStoreById(storeId));
	}

	// 가게 등록 신청 상태 변경
	@Secured("ROLE_ADMIN")
	@PutMapping("/status/request")
	public ResponseEntity<StoreAdminResponse> updateStoreRequestStatus(
		@RequestBody UpdateStoreStatusRequest request) {

		adminStoreService.updateStoreRequestStatus(request);

		return ResponseEntity.ok(adminStoreService.getStoreById(request.getStoreId()));
	}
}
