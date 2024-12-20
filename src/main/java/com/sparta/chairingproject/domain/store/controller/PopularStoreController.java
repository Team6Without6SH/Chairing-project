package com.sparta.chairingproject.domain.store.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.domain.store.dto.PopularStoreResponse;
import com.sparta.chairingproject.domain.store.service.PopularStoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/popular-stores")
@RequiredArgsConstructor
public class PopularStoreController {

	private final PopularStoreService popularStoreService;

	@GetMapping
	public List<PopularStoreResponse> getPopularStores() {
		return popularStoreService.getPopularStores();
	}
}
