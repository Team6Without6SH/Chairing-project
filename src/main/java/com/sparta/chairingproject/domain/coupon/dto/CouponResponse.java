package com.sparta.chairingproject.domain.coupon.dto;

import lombok.Builder;

@Builder
public record CouponResponse(
        Long id,
        String name,
        int quantity,
        int discountPrice,
        String createAt
) {
}
