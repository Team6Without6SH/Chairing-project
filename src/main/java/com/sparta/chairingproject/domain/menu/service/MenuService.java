package com.sparta.chairingproject.domain.menu.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.dto.request.MenuRequest;
import com.sparta.chairingproject.domain.menu.dto.request.MenuUpdateRequest;
import com.sparta.chairingproject.domain.menu.dto.response.MenuDeleteResponse;
import com.sparta.chairingproject.domain.menu.dto.response.MenuDetailResponse;
import com.sparta.chairingproject.domain.menu.dto.response.MenuResponse;
import com.sparta.chairingproject.domain.menu.dto.response.MenuUpdateResponse;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.entity.MenuStatus;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuService {

	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;

	@Transactional
	@CacheEvict(value = "menus", key = "'store:' + #storeId + ':menus'")
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

	@Transactional
	@CacheEvict(value = "menus", key = "'store:' + #storeId + ':menus'")
	public MenuUpdateResponse updateMenu(Long storeId, Long menuId, MenuUpdateRequest request, Member member) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		if (!store.getOwner().getId().equals(member.getId())) {
			throw new GlobalException(ONLY_OWNER_ALLOWED);
		}
		Menu menu = menuRepository.findByIdAndStore(menuId, store)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_MENU));
		// 메뉴 정보 업데이트
		if (request.getName() != null) {
			menu.updateName(request.getName());
		}
		if (request.getPrice() != null) {
			menu.updatePrice(request.getPrice());
		}
		if (request.getStatus() != null) {
			menu.updateStatus(request.getStatus());
		}
		return MenuUpdateResponse.from(menu);
	}

	@Transactional
	@CacheEvict(value = "menus", key = "'store:' + #storeId + ':menus'")
	public MenuDeleteResponse deleteMenu(Long storeId, Long menuId, Member member, RequestDto request) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		if (!store.getOwner().getId().equals(member.getId())) {
			throw new GlobalException(ONLY_OWNER_ALLOWED);
		}
		Menu menu = menuRepository.findByIdAndStatus(menuId, MenuStatus.DELETED)
			.orElseThrow(() -> new GlobalException(ONLY_DELETED_MENU_CAN_BE_REMOVED));
		menu.delete();

		return MenuDeleteResponse.from(menu);
	}

	@Cacheable(value = "menus", key = "'store:' + #storeId + ':menus'")
	@Transactional(readOnly = true)
	public List<MenuDetailResponse> getAllMenusByStore(Long storeId, Member member) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		if (!store.getOwner().getId().equals(member.getId())) {
			throw new GlobalException(ONLY_OWNER_ALLOWED);
		}

		List<Menu> menus = menuRepository.findAllByStoreId(storeId);

		return menus.stream()
			.map(MenuDetailResponse::from)
			.toList();
	}
}
