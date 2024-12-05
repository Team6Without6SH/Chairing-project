package com.sparta.chairingproject.domain.store.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.service.StoreService;
import com.sparta.chairingproject.util.ResponseBodyDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreController {

	private final StoreService storeService;

	@PostMapping("/owners/stores/register")
	public ResponseEntity<Void> registerStore(
		@Valid @RequestBody StoreRequest storeRequest,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		storeService.registerStore(storeRequest, authMember);
		return ResponseEntity.ok().build();
	}
}