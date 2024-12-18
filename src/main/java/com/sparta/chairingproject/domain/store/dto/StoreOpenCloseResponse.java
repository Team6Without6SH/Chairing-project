package com.sparta.chairingproject.domain.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreOpenCloseResponse {
	private Long storeId;
	private String status;
}
