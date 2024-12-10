package com.sparta.chairingproject.domain.store.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UpdateStoreStatusRequest {

	@NotNull(message = "Store ID는 필수입니다.")
	private Long storeId;

	@NotBlank(message = "상태는 필수 입력 항목입니다.")
	private String status; // APPROVED, REJECTED 등

	//테스트용 가게상태 변경 성공 테스트
	public UpdateStoreStatusRequest(long l, String approved) {
		this.storeId = l;
		this.status = approved;
	}
}
