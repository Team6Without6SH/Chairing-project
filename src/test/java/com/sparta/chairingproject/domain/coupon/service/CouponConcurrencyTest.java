package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
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
	private Member member;
	private int threadCount;
	private ExecutorService executorService;
	private CountDownLatch latch;

	@BeforeEach
	void setUp() {
		member = new Member("Test User", "test@example.com", "password", MemberRole.USER);
		memberRepository.save(member);

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

	// @Test
	// @DisplayName("동시성 문제 발생 여부 확인")
	// void issueCoupon_concurrentAccess() throws InterruptedException {
	//
	// 	for (int i = 0; i < threadCount; i++) {
	// 		executorService.submit(() -> {
	// 			try {
	// 				couponService.issueCoupon(coupon.getId(), new RequestDto(member.getId()), member);
	// 			} catch (GlobalException e) {
	// 				System.out.println("예외 발생: " + e.getMessage());
	// 			} finally {
	// 				latch.countDown();
	// 			}
	// 		});
	// 	}
	//
	// 	latch.await();
	//
	// 	// 결과 검증
	// 	Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
	// 	System.out.println("남은 쿠폰 수량: " + updatedCoupon.getQuantity());
	// 	System.out.println("발급된 쿠폰 수량: " + issuanceRepository.count());
	// }

	@Test
	@DisplayName("비관적 락 검증 테스트")
	void pessimisticLockTest() {
		Coupon lockedCoupon = couponRepository.findByIdWithPessimisticLock(coupon.getId())
			.orElseThrow(() -> new GlobalException(COUPON_NOT_FOUND));

		assertNotNull(lockedCoupon);
		System.out.println("Locked Coupon ID: " + lockedCoupon.getId());
	}

	@Test
	@DisplayName("비관적 락 동시성 제어 테스트")
	void synchronizedBlockConcurrencyTest() throws InterruptedException {

		for (int i = 0; i < threadCount; i++) {
			int threadId = i;
			executorService.submit(() -> {
				try {
					System.out.println("Thread 시작: " + threadId);
					Member threadMember = new Member("Test User" + threadId, "test" + threadId + "@example.com",
						"password", MemberRole.USER);
					memberRepository.save(threadMember);
					couponService.issueCoupon(coupon.getId(), new RequestDto(threadMember.getId()), threadMember);
				} catch (GlobalException e) {
					System.out.println("Thread 예외: " + threadId + ", " + e.getMessage());
				} finally {
					latch.countDown();
					System.out.println("Thread 완료: " + threadId);
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
