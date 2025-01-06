package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;

@SpringBootTest
@Transactional
class CouponServiceTest {

	@Autowired
	private CouponService couponService;

	@Autowired
	private CouponRepository couponRepository;

	@Autowired
	private IssuanceRepository issuanceRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Long couponId;
	private Member member;
	private CouponRequest validRequest;
	private RequestDto request;

	@BeforeEach
	void setUp() {
		member = new Member("Test User", "test@example.com", "test-password","image", MemberRole.USER);
		member = memberRepository.save(member);

		Coupon coupon = Coupon.builder()
			.name("Discount10")
			.quantity(100)
			.discountPrice(10)
			.build();
		coupon = couponRepository.save(coupon);

		couponId = coupon.getId();
		validRequest = new CouponRequest("Discount10", 100, 10);
		request = new RequestDto(member.getId());
	}

	@AfterEach
	void tearDown() {
		issuanceRepository.deleteAll();
		couponRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	@DisplayName("쿠폰 생성 성공 - 중복되지 않은 이름")
	void createCoupon_success() {
		// Given
		CouponRequest newRequest = new CouponRequest("UniqueName", 100, 10);

		// When
		CouponResponse response = couponService.createCoupon(newRequest);

		// Then
		assertNotNull(response);
		assertEquals(newRequest.getName(), response.name());
		assertTrue(couponRepository.existsByName(newRequest.getName()));
	}

	@Test
	@DisplayName("쿠폰 발급 성공")
	void issueCoupon_success() {
		// When
		couponService.issueCoupon(couponId, request, member);

		// Then
		Coupon coupon = couponRepository.findById(couponId).orElseThrow();
		assertEquals(99, coupon.getQuantity());
		assertEquals(1, issuanceRepository.count());
	}

	@Test
	@DisplayName("쿠폰 생성 실패 - 중복된 이름")
	void createCoupon_fail_duplicateName() {
		// Given
		CouponRequest duplicateRequest = new CouponRequest("Discount10", 100, 10);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> couponService.createCoupon(duplicateRequest));

		assertEquals(COUPON_NAME_ALREADY_EXISTS.getMessage(), exception.getMessage());
	}

	@Test
	@DisplayName("쿠폰 발급 실패 - 쿠폰 없음")
	void issueCoupon_fail_couponNotFound() {
		// Given
		Long invalidCouponId = 999L;

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> couponService.issueCoupon(invalidCouponId, request, member));

		assertEquals(COUPON_NOT_FOUND.getMessage(), exception.getMessage());
	}

	@Test
	@DisplayName("쿠폰 발급 실패 - 이미 발급받은 쿠폰")
	void issueCoupon_fail_alreadyIssued() {
		// Given
		couponService.issueCoupon(couponId, request, member);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> couponService.issueCoupon(couponId, request, member));

		assertEquals(COUPON_ALREADY_ISSUED.getMessage(), exception.getMessage());
	}

	@Test
	@DisplayName("쿠폰 전체 조회 성공")
	void getAllCoupons_success() {
		// Given
		PageRequest pageRequest = PageRequest.of(0, 10);

		// When
		Page<CouponResponse> response = couponService.getAllCoupons(new RequestDto(member.getId()), pageRequest);

		// Then
		assertNotNull(response);
		assertEquals(1, response.getTotalElements());
		assertEquals("Discount10", response.getContent().get(0).name());
	}
}
