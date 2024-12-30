package com.sparta.chairingproject.domain.coupon.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

	@Mock
	private CouponRepository couponRepository;

	@Mock
	private IssuanceRepository issuanceRepository;

	@InjectMocks
	private CouponService couponService;

	private Long couponId;
	private Member member;
	private CouponRequest validRequest;
	private RequestDto request;

	@BeforeEach
	void setUp() {
		couponId = 1L;
		validRequest = new CouponRequest("Discount10", 100, 10);
		member = new Member("Test User", "test@example.com", "test-password", "image",
			MemberRole.USER);
	}

	@Test
	@DisplayName("쿠폰 생성 성공 - 중복되지 않은 이름")
	void createCoupon_success() {
		// Given
		when(couponRepository.existsByName(validRequest.getName())).thenReturn(false);

		// When
		CouponResponse response = couponService.createCoupon(validRequest);

		// Then
		assertNotNull(response);
		assertEquals(validRequest.getName(), response.name());
		verify(couponRepository, times(1)).existsByName(validRequest.getName());
		verify(couponRepository, times(1)).save(any(Coupon.class));
	}

	@Test
	@DisplayName("쿠폰 발급 성공")
	void issueCoupon_success() {
		// Given
		Coupon coupon = Coupon.builder()
			.id(couponId)
			.name("Spring Sale")
			.quantity(10)
			.discountPrice(500)
			.build();

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
		when(issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId)).thenReturn(
			Optional.empty());

		// When
		couponService.issueCoupon(couponId, request, member);

		// Then
		verify(couponRepository, times(1)).findById(couponId);
		verify(issuanceRepository, times(1)).findByMemberIdAndCouponId(member.getId(), couponId);
		verify(issuanceRepository, times(1)).save(any(Issuance.class));
		assertEquals(9, coupon.getQuantity());
	}

	@Test
	@DisplayName("쿠폰 생성 실패 - 중복된 이름")
	void createCoupon_fail_duplicateName() {
		// Given
		when(couponRepository.existsByName(validRequest.getName())).thenReturn(true);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> couponService.createCoupon(validRequest));

		assertEquals(COUPON_NAME_ALREADY_EXISTS.getMessage(), exception.getMessage());
		verify(couponRepository, times(1)).existsByName(validRequest.getName());
		verify(couponRepository, never()).save(any(Coupon.class));
	}

	@Test
	@DisplayName("쿠폰 발급 실패 - 쿠폰 없음")
	void issueCoupon_fail_couponNotFound() {
		// Given
		when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> couponService.issueCoupon(couponId, request, member));
		assertEquals(ExceptionCode.COUPON_NOT_FOUND.getMessage(), exception.getMessage());
		verify(couponRepository, times(1)).findById(couponId);
	}

	@Test
	@DisplayName("쿠폰 발급 실패 - 이미 발급받은 쿠폰")
	void issueCoupon_fail_alreadyIssued() {
		// Given
		Coupon coupon = Coupon.builder()
			.id(couponId)
			.name("Spring Sale")
			.quantity(10)
			.discountPrice(500)
			.build();

		when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
		when(issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId)).thenReturn(
			Optional.of(new Issuance()));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> couponService.issueCoupon(couponId, request, member));
		assertEquals(ExceptionCode.COUPON_ALREADY_ISSUED.getMessage(), exception.getMessage());
		verify(couponRepository, times(1)).findById(couponId);
		verify(issuanceRepository, times(1)).findByMemberIdAndCouponId(member.getId(), couponId);
	}

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	@DisplayName("쿠폰 전체 조회 성공")
	void getAllCoupons_success() {
		// Given
		PageRequest pageRequest = PageRequest.of(1, 10);

		Coupon coupon1 = Coupon.builder()
			.id(1L)
			.name("Spring Sale")
			.quantity(50)
			.discountPrice(5000)
			.build();

		Coupon coupon2 = Coupon.builder()
			.id(2L)
			.name("Winter Sale")
			.quantity(30)
			.discountPrice(10000)
			.build();

		when(couponRepository.findAll(pageRequest))
			.thenReturn(new PageImpl<>(List.of(coupon1, coupon2)));

		// When
		Page<CouponResponse> response = couponService.getAllCoupons(new RequestDto(1L),
			pageRequest);

		// Then
		assertNotNull(response);
		assertEquals(2, response.getTotalElements());
		assertEquals("Spring Sale", response.getContent().get(0).name());
		assertEquals("Winter Sale", response.getContent().get(1).name());
		assertEquals(50, response.getContent().get(0).quantity());
		assertEquals(5000, response.getContent().get(0).discountPrice());
		verify(couponRepository, times(1)).findAll(pageRequest);
	}
}