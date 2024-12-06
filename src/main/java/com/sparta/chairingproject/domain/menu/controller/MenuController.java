package com.sparta.chairingproject.domain.menu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.menu.dto.request.MenuRequest;
import com.sparta.chairingproject.domain.menu.dto.response.MenuDeleteResponse;
import com.sparta.chairingproject.domain.menu.dto.response.MenuResponse;
import com.sparta.chairingproject.domain.menu.service.MenuService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/owners/stores/{storeId}")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	@Secured("ROLE_OWNER")
	@PostMapping("/menus")
	public ResponseEntity<MenuResponse> createMenu(
		@PathVariable Long storeId,
		@Valid @RequestBody MenuRequest request,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		return ResponseEntity.ok(menuService.createMenu(storeId, request, authMember.getMember()));
	}

	@Secured("ROLE_OWNER")
	@DeleteMapping("/menus/{menuId}")
	public ResponseEntity<MenuDeleteResponse> deleteMenu(
		@PathVariable Long storeId,
		@PathVariable Long menuId,
		@AuthenticationPrincipal UserDetailsImpl authMember,
		@RequestBody RequestDto request
	) {
		return ResponseEntity.ok(menuService.deleteMenu(storeId, menuId, authMember.getMember(), request));
	}
}
