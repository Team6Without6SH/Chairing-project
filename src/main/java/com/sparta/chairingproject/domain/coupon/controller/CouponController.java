package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.Issuance.dto.IssuanceResponse;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @Secured("ROLE_ADMIN") // ADMIN 권한만 접근 가능
    @PostMapping("/coupons")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping("/coupons/{couponId}")
    public ResponseEntity<Void> issueCoupon(@PathVariable Long couponId, @AuthenticationPrincipal UserDetailsImpl authMember) {
        couponService.issueCoupon(couponId, authMember.getMember());
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_USER")
    @GetMapping("/members/coupons")
    public ResponseEntity<List<IssuanceResponse>> getIssuedCoupons(@AuthenticationPrincipal UserDetailsImpl authmember) {
        List<IssuanceResponse> response = couponService.getIssuedCoupons(authmember.getMember());
        return ResponseEntity.ok(response);
    }

}
