package com.sparta.chairingproject.domain.menu.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.dto.request.MenuRequest;
import com.sparta.chairingproject.domain.menu.dto.response.MenuResponse;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	@Transactional
	public MenuResponse createMenu(Long storeId, @Valid MenuRequest request, Member member) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_MENU));
		if (!store.getOwner().getId().equals(member.getId())) {
			throw new GlobalException(ONLY_OWNER_ALLOWED);
		}
		boolean isDuplicatedMenu = menuRepository.existsByStoreAndName(store, request.getName());
		if (isDuplicatedMenu) {
			throw new GlobalException(DUPLICATED_MENU);
		}
		Menu menu = Menu.createOf(request.getName(), request.getPrice(), request.getImage(), store);
		menuRepository.save(menu);
		return MenuResponse.from(menu);
	}
}
