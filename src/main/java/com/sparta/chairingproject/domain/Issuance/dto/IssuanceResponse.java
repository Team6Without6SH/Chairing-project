package com.sparta.chairingproject.domain.Issuance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class IssuanceResponse {
    private Long couponId;
    private String name;
    private int discountPrice;
    private LocalDateTime issuedAt;
}
