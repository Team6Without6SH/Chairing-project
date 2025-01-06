package com.sparta.chairingproject.domain.coupon.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;

@SpringBootTest
public class CouponConcurrencyTest {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private IssuanceRepository issuanceRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Coupon coupon;
	private int threadCount;
	private ExecutorService executorService;
	private CountDownLatch latch;

	@BeforeEach
	void setUp() {
		coupon = Coupon.builder()
			.name("Test Coupon")
			.quantity(100)
			.discountPrice(500)
			.build();
		couponRepository.save(coupon);
		threadCount = 100;
		executorService = Executors.newFixedThreadPool(threadCount);
		latch = new CountDownLatch(threadCount);
	}

	@AfterEach
	void tearDown() {
		issuanceRepository.deleteAll();
		couponRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	@DisplayName("Redis 락 동시성 제어 & 순서 제어 테스트")
	void redisLockConcurrencyTest() throws InterruptedException {
		for (int i = 0; i < threadCount; i++) {
			int threadId = i;
			executorService.submit(() -> {
				try {
					Member threadMember = new Member("Test User" + threadId, "test" + threadId + "@example.com",
						"password", null, MemberRole.USER);
					memberRepository.save(threadMember);
					couponService.issueCoupon(coupon.getId(), new RequestDto(threadMember.getId()), threadMember);
				} catch (GlobalException e) {
					System.out.println("Thread 예외: " + Thread.currentThread().getId() + ", " + e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();

		// 결과 검증
		Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
		System.out.println("남은 쿠폰 수량: " + updatedCoupon.getQuantity());
		System.out.println("발급된 쿠폰 수량: " + issuanceRepository.count());

		assertEquals(0, updatedCoupon.getQuantity());
		assertEquals(100, issuanceRepository.count());
	}
}
