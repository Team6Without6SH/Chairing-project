package com.sparta.chairingproject.domain.coupon.dto;

import com.sparta.chairingproject.domain.common.dto.RequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest extends RequestDto {

        @NotNull(message = "쿠폰 이름이 비었습니다.")
        private String name;

        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private int quantity;

        @Min(value = 1, message = "할인 가격은 1 이상이어야 합니다.")
        private int discountPrice;
}
