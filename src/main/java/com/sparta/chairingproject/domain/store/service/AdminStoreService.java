package com.sparta.chairingproject.domain.store.service;

import java.util.List;

import org.springframework.stereotype.Service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.store.dto.StoreResponseAdmin;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.mapper.StoreMapper;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStoreService {

	private final StoreRepository storeRepository;

	// 모든 가게 조회
	public List<StoreResponseAdmin> getAllStores() {
		List<Store> stores = storeRepository.findAll();
		if (stores.isEmpty()) {
			throw new GlobalException(NOT_FOUND_STORE);
		}
		// StoreMapper 를 사용하여 변환
		return StoreMapper.toAdminStoreResponseList(stores);
	}

	// 단일 가게 조회
	public StoreResponseAdmin getStoreById(Long storeId) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		return StoreMapper.toAdminStoreResponse(store);
	}


}

