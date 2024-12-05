package com.sparta.chairingproject.domain.coupon.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.service.CouponService;
import com.sparta.chairingproject.util.ResponseBodyDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Secured("ROLE_USER")
    @PostMapping("/{couponId}")
    public ResponseEntity<Void> issueCoupon(@PathVariable Long couponId, @RequestBody RequestDto request, @AuthenticationPrincipal UserDetailsImpl authMember) {
        couponService.issueCoupon(couponId, request, authMember.getMember());
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_ADMIN")
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @RequestBody RequestDto request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<CouponResponse> response = couponService.getAllCoupons(request, pageRequest);
        return ResponseEntity.ok(response);
    }
}
