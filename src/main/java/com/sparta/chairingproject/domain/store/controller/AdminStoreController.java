package com.sparta.chairingproject.domain.store.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {

	private final StoreService storeService;

	@Secured("ROLE_ADMIN")
	@GetMapping
	public ResponseEntity<List<StoreResponse>> getAdminStores(
		@RequestParam(required = false) StoreStatus status
	) {
		List<StoreResponse> stores = storeService.getAdminStores(status);
		return ResponseEntity.ok(stores);
	}
}
