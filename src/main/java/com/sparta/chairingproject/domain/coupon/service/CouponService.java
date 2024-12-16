package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
	private final RedissonClient redissonClient;

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
	public void issueCoupon(Long couponId, RequestDto request, Member member) {
		// Redis 락
		String lockKey = "coupon:" + couponId;
		RLock lock = redissonClient.getLock(lockKey);

		try {
			// 락 획득 시도 (최대 5초 대기, 10초 유지)
			if (!lock.tryLock(1, 10, TimeUnit.SECONDS)) {
				System.out.println("Thread " + Thread.currentThread().getId() + "락 획득 실패.");
				throw new IllegalStateException("동시에 너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.");
			}
			System.out.println("Thread " + Thread.currentThread().getId() + "락 획득.");

			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCompletion(int status) {
					if (lock.isHeldByCurrentThread()) {
						lock.unlock();
						System.out.println("Thread " + Thread.currentThread().getId() + " released lock.");
					}
				}
			});

			// sampleService.issue(couponId, request, member);
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
		} catch (InterruptedException e) {
			System.out.println("Thread " + Thread.currentThread().getId() + " was interrupted.");
			throw new IllegalArgumentException("Redis 락을 획득하는데 실패했습니다.", e);
		}
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