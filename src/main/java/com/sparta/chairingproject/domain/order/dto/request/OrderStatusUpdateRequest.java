package com.sparta.chairingproject.domain.order.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class OrderStatusUpdateRequest extends RequestDto {
	@NotEmpty(message = "예약 상태는 필수 입력 항목입니다.")
	private String status;

	//테스트 코드용 생성자
	public OrderStatusUpdateRequest(String status) {
		this.status = status;
	}
}
