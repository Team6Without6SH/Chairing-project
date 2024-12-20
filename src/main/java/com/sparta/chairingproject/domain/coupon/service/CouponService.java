package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
	private final RedisTemplate<String, String> redisTemplate; // FIFO 큐 역할

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
		String lockKey = "coupon:" + couponId;
		String sortedSetKey = "coupon_queue:" + couponId;
		RLock lock = redissonClient.getLock(lockKey);
		long currentTime = System.nanoTime(); // 요청 순서를 보장하기 위한 타임스탬프
		long threadId = Thread.currentThread().getId();
		long uniqueTimestamp = currentTime + threadId;

		try {
			// 락 획득 시도 (최대 5초 대기, 10초 유지)
			if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("동시에 너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.");
			}

			// Lua 스크립트로 원자적 작업 수행
			String luaScript = """
				    local sortedSetKey = KEYS[1]
				    local memberId = ARGV[1]
				    local timestamp = ARGV[2]

				    redis.call('ZADD', sortedSetKey, timestamp, memberId)
				    local minMember = redis.call('ZRANGE', sortedSetKey, 0, 0)[1]

				    if minMember == memberId then
				        return true
				    else
				        return false
				    end
				""";

			DefaultRedisScript<Boolean> script = new DefaultRedisScript<>();
			script.setScriptText(luaScript);
			script.setResultType(Boolean.class);

			Boolean isEligible = redisTemplate.execute(
				script,
				Collections.singletonList(sortedSetKey),
				String.valueOf(member.getId()), String.valueOf(uniqueTimestamp) // uniqueTimestamp 전달
			);

			if (Boolean.FALSE.equals(isEligible)) {
				throw new IllegalStateException("선착순이 아니므로 쿠폰을 발급할 수 없습니다.");
			}

			// 쿠폰 발급 처리
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

			// 발급 완료 후 요청 삭제
			redisTemplate.opsForZSet().remove(sortedSetKey, String.valueOf(member.getId()));
		} catch (InterruptedException e) {
			throw new IllegalArgumentException("Redis 락을 획득하는데 실패했습니다.", e);
		} finally {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
				@Override
				public void afterCompletion(int status) {
					if (lock.isHeldByCurrentThread()) {
						lock.unlock();
						// System.out.println("Thread " + Thread.currentThread().getId() + " released lock.");
					}
				}
			});
		}
	}

	// // 초기 코드
	// @Transactional
	// public void issueCoupon(Long couponId, RequestDto request, Member member) {
	// 	// Redis 락
	// 	String lockKey = "coupon:" + couponId;
	// 	RLock lock = redissonClient.getFairLock(lockKey);
	//
	// 	try {
	// 		// 락 획득 시도 (최대 5초 대기, 10초 유지)
	// 		if (!lock.tryLock(1, 10, TimeUnit.SECONDS)) {
	// 			System.out.println("Thread " + Thread.currentThread().getId() + "락 획득 실패.");
	// 			throw new IllegalStateException("동시에 너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.");
	// 		}
	// 		// System.out.println("Thread " + Thread.currentThread().getId() + "락 획득.");
	//
	// 		Coupon coupon = couponRepository.findById(couponId)
	// 			.orElseThrow(() -> new GlobalException(COUPON_NOT_FOUND));
	//
	// 		if (issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId).isPresent()) {
	// 			throw new GlobalException(COUPON_ALREADY_ISSUED);
	// 		}
	//
	// 		coupon.validateQuantity();
	// 		coupon.decreaseQuantity();
	//
	// 		Issuance issuance = Issuance.builder()
	// 			.member(member)
	// 			.coupon(coupon)
	// 			.build();
	// 		issuanceRepository.save(issuance);
	// 	} catch (InterruptedException e) {
	// 		System.out.println("Thread " + Thread.currentThread().getId() + " was interrupted.");
	// 		throw new IllegalArgumentException("Redis 락을 획득하는데 실패했습니다.", e);
	// 	} finally {
	// 		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
	// 			@Override
	// 			public void afterCompletion(int status) {
	// 				if (lock.isHeldByCurrentThread()) {
	// 					lock.unlock();
	// 					System.out.println("Thread " + Thread.currentThread().getId() + " released lock.");
	// 				}
	// 			}
	// 		});
	// 	}
	// }

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