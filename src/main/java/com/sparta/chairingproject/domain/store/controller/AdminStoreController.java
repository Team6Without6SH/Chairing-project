package com.sparta.chairingproject.domain.store.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.sparta.chairingproject.domain.store.dto.StoreResponseAdmin;
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
	public ResponseEntity<List<StoreResponseAdmin>> getAllStores() {
		List<StoreResponseAdmin> stores = adminStoreService.getAllStores();
		return ResponseEntity.ok(stores);
	}

	// 단일 가게 조회
	@Secured("ROLE_ADMIN")
	@GetMapping("/{storeId}")
	public ResponseEntity<StoreResponseAdmin> getStoreById(@PathVariable Long storeId) {
		StoreResponseAdmin store = adminStoreService.getStoreById(storeId);
		return ResponseEntity.ok(store);
	}
}
