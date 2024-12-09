package com.sparta.chairingproject.domain.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CloseStoreRequest {
	@NotNull(message = "Store ID는 필수입니다.")
	private Long storeId;
}
