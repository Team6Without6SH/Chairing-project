package com.sparta.chairingproject.domain.store.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.store.dto.StoreAdminResponse;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreStatusRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.mapper.StoreMapper;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStoreService {

	private final StoreRepository storeRepository;

	// 모든 가게 조회
	public List<StoreAdminResponse> getAllStores() {
		List<Store> stores = storeRepository.findAll();
		if (stores.isEmpty()) {
			throw new GlobalException(NOT_FOUND_STORE);
		}
		// StoreMapper 를 사용하여 변환
		return StoreMapper.toAdminStoreResponseList(stores);
	}

	// 단일 가게 조회
	public StoreAdminResponse getStoreById(Long storeId) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		return StoreMapper.toAdminStoreResponse(store);
	}

	@Transactional
	// 가게 상태
	public StoreAdminResponse updateStoreRequestStatus(UpdateStoreStatusRequest request) {

		Store store = storeRepository.findById(request.getStoreId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		StoreRequestStatus requestStatus = StoreRequestStatus.valueOf(request.getStatus());

		switch (requestStatus) {
			case APPROVED -> store.approveRequest();
			case REJECTED -> store.rejectRequest();
			case DELETE_REQUESTED -> {
				// DELETE_REQUESTED 는 허용되지 않는 상태
				throw new GlobalException(INVALID_REQUEST_STATUS);
			}
			case DELETED -> store.approveDelete();
			case DELETE_REJECTED -> store.rejectDeleteRequest();
			default -> throw new GlobalException(INVALID_REQUEST_STATUS);
		}

		storeRepository.save(store);
		return new StoreAdminResponse(store.getId(), store.getName(), store.getStatus().name(),
			store.getRequestStatus().name());
	}

	@Transactional
	public void approveCloseStore(Long storeId) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		if (store.getRequestStatus() != StoreRequestStatus.DELETE_REQUESTED) {
			throw new GlobalException(INVALID_REQUEST_STATUS);
		}

		store.approveDelete();
		storeRepository.save(store);
	}
}

