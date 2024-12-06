package com.sparta.chairingproject.domain.coupon.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
public record CouponResponse(
        Long id,
        String name,
        int quantity,
        int discountPrice,
        LocalDateTime createAt
) {
}
