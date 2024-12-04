package com.sparta.chairingproject.domain.coupon.service;

import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponResponse createCoupon(CouponRequest request, Member member) {
        Coupon coupon = Coupon.builder()
                .name(request.name())
                .quantity(request.quantity())
                .discountPrice(request.discountPrice())
                .build();

        couponRepository.save(coupon);

        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .quantity(coupon.getQuantity())
                .discountPrice(coupon.getDiscountPrice())
                .createAt(coupon.getCreatedAt())
                .build();
    }
}