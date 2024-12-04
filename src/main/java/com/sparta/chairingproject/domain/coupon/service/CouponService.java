package com.sparta.chairingproject.domain.coupon.service;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.util.ResponseBodyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final IssuanceRepository issuanceRepository;

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

    // TODO: 글로벌 예외처리
    public ResponseBodyDto<String> issueCoupon(Long couponId, Member member) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰을 찾을 수 없습니다. ID: " + couponId));

        if (!MemberRole.USER.equals(member.getMemberRole())) {
            throw new IllegalStateException("쿠폰 발급 권한이 없습니다.");
        }

        if (issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId).isPresent()) {
            throw new IllegalStateException("이미 해당 쿠폰을 발급받았습니다.");
        }

        if (coupon.getQuantity() <= 0) {
            throw new IllegalStateException("쿠폰 수량이 부족합니다.");
        }
        coupon.updateQuantity(coupon.getQuantity() - 1);

        Issuance issuance = Issuance.builder()
                .member(member)
                .coupon(coupon)
                .build();
        issuanceRepository.save(issuance);

        return ResponseBodyDto.success("쿠폰이 성공적으로 발급되었습니다.");
    }
}