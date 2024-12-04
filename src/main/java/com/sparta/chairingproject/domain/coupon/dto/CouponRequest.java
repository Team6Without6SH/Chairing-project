package com.sparta.chairingproject.domain.coupon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CouponRequest(
        @NotNull(message = "쿠폰 이름이 비었습니다.")
        String name,
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        int quantity,
        @Min(value = 0, message = "할인 가격은 0 이상이어야 합니다.")
        int discountPrice
) {
}
