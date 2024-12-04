package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.service.CouponService;
import com.sparta.chairingproject.util.ResponseBodyDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @Secured("ROLE_ADMIN") // ADMIN 권한만 접근 가능
    @PostMapping
    public ResponseEntity<ResponseBodyDto<CouponResponse>> createCoupon(@Valid @RequestBody CouponRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CouponResponse response = couponService.createCoupon(request, userDetails.getMember());
        return ResponseEntity.ok(ResponseBodyDto.success("쿠폰 생성 성공", response));
    }

    @Secured("ROLE_USER")
    @PostMapping("/{couponId}")
    public ResponseEntity<ResponseBodyDto<String>> issueCoupon(@PathVariable Long couponId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ResponseBodyDto<String> response = couponService.issueCoupon(couponId, userDetails.getMember());
        return ResponseEntity.ok(response);
    }
}
