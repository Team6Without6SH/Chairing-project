package com.sparta.chairingproject.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StoreOpenCloseRequest {
	@NotBlank(message = "상태는 필수 입력값입니다.")
	private String status;
}
