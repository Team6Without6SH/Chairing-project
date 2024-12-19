package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponService {
	private final CouponRepository couponRepository;
	private final IssuanceRepository issuanceRepository;
	private final EntityManager entityManager;

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

	@Transactional
	@Retryable(
		value = {ObjectOptimisticLockingFailureException.class},
		maxAttempts = 100,
		backoff = @Backoff(delay = 50) // 500ms 간격으로 재시도
	)
	public void issueCoupon(Long couponId, RequestDto request, Member member) {
		System.out.println("재시도 횟수: " + RetrySynchronizationManager.getContext().getRetryCount());
		// 디비 단에서 낙관적 락 적용
		Coupon coupon = couponRepository.findByIdWithOptimisticLock(couponId)
			.orElseThrow(() -> new GlobalException(COUPON_NOT_FOUND));

		// if (issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId).isPresent()) {
		// 	throw new GlobalException(COUPON_ALREADY_ISSUED);
		// }

		coupon.validateQuantity();
		coupon.decreaseQuantity();

		// 변경 사항 저장
		couponRepository.saveAndFlush(coupon);

		Issuance issuance = Issuance.builder()
			.member(member)
			.coupon(coupon)
			.build();
		issuanceRepository.save(issuance);

		System.out.println("쿠폰 발급 성공. 쿠폰 버전: " + coupon.getVersion());
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