package com.sparta.chairingproject.domain.coupon.service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.Issuance.dto.IssuanceResponse;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.COUPON_ALREADY_ISSUED;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.COUPON_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final IssuanceRepository issuanceRepository;

    public CouponResponse createCoupon(CouponRequest request) {
        Coupon coupon = Coupon.builder()
                .name(request.getName())
                .quantity(request.getQuantity())
                .discountPrice(request.getDiscountPrice())
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

    public void issueCoupon(Long couponId, Member member) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new GlobalException(COUPON_NOT_FOUND));

        if (issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId).isPresent()) {
            throw new GlobalException(COUPON_ALREADY_ISSUED);
        }

        coupon.validateQuantity();
        coupon.decreaseQuantity();

        Issuance issuance = Issuance.builder()
                .member(member)
                .coupon(coupon)
                .build();
        issuanceRepository.save(issuance);
    }

    public List<IssuanceResponse> getIssuedCoupons(Member member) {
        List<Issuance> issuances = issuanceRepository.findAllByMemberId(member.getId());

        return issuances.stream()
                .map(issuance -> new IssuanceResponse(
                        issuance.getCoupon().getId(),
                        issuance.getCoupon().getName(),
                        issuance.getCoupon().getDiscountPrice(),
                        issuance.getCoupon().getCreatedAt()
                )).toList();
    }
}