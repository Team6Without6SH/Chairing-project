package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.service.CouponService;
import com.sparta.chairingproject.util.ResponseBodyDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request, @AuthenticationPrincipal UserDetailsImpl authMember) {
        CouponResponse response = couponService.createCoupon(request, authMember.getMember());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{couponId}")
    public ResponseEntity<Void> issueCoupon(@PathVariable Long couponId, @AuthenticationPrincipal UserDetailsImpl authMember) {
        couponService.issueCoupon(couponId, authMember.getMember());
        return ResponseEntity.ok().build();
    }
}
