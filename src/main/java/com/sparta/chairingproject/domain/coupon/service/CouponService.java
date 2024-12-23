package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {
	private final CouponRepository couponRepository;
	private final IssuanceRepository issuanceRepository;

	public CouponResponse createCoupon(CouponRequest request) {
		if (couponRepository.existsByName(request.getName())) {
			throw new GlobalException(COUPON_NAME_ALREADY_EXISTS);
		}

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

	public void issueCoupon(Long couponId, RequestDto request, Member member) {
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

	public Page<CouponResponse> getAllCoupons(RequestDto request, PageRequest pageRequest) {
		Page<Coupon> coupons = couponRepository.findAll(pageRequest);

		return coupons.map(coupon -> CouponResponse.builder()
			.id(coupon.getId())
			.name(coupon.getName())
			.quantity(coupon.getQuantity())
			.discountPrice(coupon.getDiscountPrice())
			.createAt(coupon.getCreatedAt())
			.build());
	}
}