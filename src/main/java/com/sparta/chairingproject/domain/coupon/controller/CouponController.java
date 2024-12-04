package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.service.CouponService;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.util.ResponseBodyDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<ResponseBodyDto<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CouponResponse response = couponService.createCoupon(request, userDetails.getMember());
        return ResponseEntity.ok(ResponseBodyDto.success("쿠폰 생성 성공", response));
    }
}
