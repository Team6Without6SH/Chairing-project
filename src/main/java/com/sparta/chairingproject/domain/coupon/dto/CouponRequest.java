package com.sparta.chairingproject.domain.coupon.dto;

import com.sparta.chairingproject.domain.common.dto.RequestDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest extends RequestDto {

	@NotBlank(message = "쿠폰 이름이 비었습니다.")
	private String name;

	@Min(value = 1, message = "수량은 1 이상이어야 합니다.")
	@Max(value = 1000, message = "수량은 최대 1000개까지 가능합니다.")
	private int quantity;

	@Min(value = 1, message = "할인 가격은 1 이상이어야 합니다.")
	@Max(value = 100000, message = "할인 금액은 최대 100,000원 까지 가능합니다.")
	private int discountPrice;
}
