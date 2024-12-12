package com.sparta.chairingproject.domain.store.mapper;

import com.sparta.chairingproject.domain.store.dto.StoreAdminResponse;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.entity.Store;

import java.util.List;

public class StoreMapper {

	public static List<StoreResponse> toStoreResponseList(List<Store> stores) {
		return stores.stream()
			.map(StoreMapper::toStoreResponse)
			.toList();
	}

	public static StoreResponse toStoreResponse(Store store) {
		return new StoreResponse(
			store.getId(),
			store.getName(),
			store.getAddress(),
			store.getPhone(),
			store.getOpenTime(),
			store.getCloseTime(),
			store.getCategory(),
			store.getDescription(),
			store.getImage(),
			store.getOwner().getName(),
			store.getTableCount(),
			store.getStatus().name(),
			store.getRequestStatus().name()
		);
	}

	public static StoreAdminResponse toAdminStoreResponse(Store store) {
		return new StoreAdminResponse(
			store.getId(),
			store.getName(),
			store.getOwner().getName(),
			store.getStatus().name(),
			store.getRequestStatus().name()
		);
	}

	public static List<StoreAdminResponse> toAdminStoreResponseList(List<Store> stores) {
		return stores.stream()
			.map(StoreMapper::toAdminStoreResponse) // 개별 Store -> StoreResponseAdmin 변환
			.toList();
	}
}
