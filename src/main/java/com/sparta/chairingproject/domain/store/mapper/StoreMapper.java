package com.sparta.chairingproject.domain.store.mapper;

import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.dto.StoreResponseAdmin;
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
			null, // 카테고리 정보는 확장 가능
			store.getDescription(),
			List.of(store.getImage() != null ? store.getImage() : "default.jpg"),
			store.getOwner().getName()
		);
	}

	public static StoreResponseAdmin toAdminStoreResponse(Store store) {
		return new StoreResponseAdmin(
			store.getId(),
			store.getName(),
			store.getOwner().getName(),
			store.getStatus().name(),
			store.getRequestStatus().name()
		);
	}

	public static List<StoreResponseAdmin> toAdminStoreResponseList(List<Store> stores) {
		return stores.stream()
			.map(StoreMapper::toAdminStoreResponse) // 개별 Store -> StoreResponseAdmin 변환
			.toList();
	}
}
