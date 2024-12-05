package com.sparta.chairingproject.domain.coupon.service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.Issuance.repository.IssuanceRepository;
import com.sparta.chairingproject.domain.coupon.dto.CouponRequest;
import com.sparta.chairingproject.domain.coupon.dto.CouponResponse;
import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import com.sparta.chairingproject.domain.coupon.repository.CouponRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private IssuanceRepository issuanceRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("쿠폰 생성 성공")
    void createCoupon_success() {
        // Given
        CouponRequest request = new CouponRequest("New Year Sale", 50, 1000);
        Coupon savedCoupon = Coupon.builder()
                .id(1L)
                .name("New Year Sale")
                .quantity(50)
                .discountPrice(1000)
                .build();

        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        // When
        CouponResponse response = couponService.createCoupon(request);

        // Then
        assertNotNull(response);
        assertEquals("New Year Sale", response.name());
        assertEquals(50, response.quantity());
        assertEquals(1000, response.discountPrice());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_success() {
        // Given
        Long couponId = 1L;
        Member member = new Member("Test User", "test@example.com", "test-password", MemberRole.USER);
        Coupon coupon = Coupon.builder()
                .id(couponId)
                .name("Spring Sale")
                .quantity(10)
                .discountPrice(500)
                .build();

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId)).thenReturn(Optional.empty());

        // When
        couponService.issueCoupon(couponId, member);

        // Then
        verify(couponRepository, times(1)).findById(couponId);
        verify(issuanceRepository, times(1)).findByMemberIdAndCouponId(member.getId(), couponId);
        verify(issuanceRepository, times(1)).save(any(Issuance.class));
        assertEquals(9, coupon.getQuantity());
    }

    @Test
    @DisplayName("쿠폰 발급 실패 - 쿠폰 없음")
    void issueCoupon_fail_couponNotFound() {
        // Given
        Long couponId = 1L;
        Member member = new Member("Test User", "test@example.com", "test-password", MemberRole.USER);

        when(couponRepository.findById(couponId)).thenReturn(Optional.empty());

        // When & Then
        GlobalException exception = assertThrows(GlobalException.class,
                () -> couponService.issueCoupon(couponId, member));
        assertEquals(ExceptionCode.COUPON_NOT_FOUND.getMessage(), exception.getMessage());
        verify(couponRepository, times(1)).findById(couponId);
    }

    @Test
    @DisplayName("쿠폰 발급 실패 - 이미 발급받은 쿠폰")
    void issueCoupon_fail_alreadyIssued() {
        // Given
        Long couponId = 1L;
        Member member = new Member("Test User", "test@example.com", "test-password", MemberRole.USER);
        Coupon coupon = Coupon.builder()
                .id(couponId)
                .name("Spring Sale")
                .quantity(10)
                .discountPrice(500)
                .build();

        when(couponRepository.findById(couponId)).thenReturn(Optional.of(coupon));
        when(issuanceRepository.findByMemberIdAndCouponId(member.getId(), couponId)).thenReturn(Optional.of(new Issuance()));

        // When & Then
        GlobalException exception = assertThrows(GlobalException.class,
                () -> couponService.issueCoupon(couponId, member));
        assertEquals(ExceptionCode.COUPON_ALREADY_ISSUED.getMessage(), exception.getMessage());
        verify(couponRepository, times(1)).findById(couponId);
        verify(issuanceRepository, times(1)).findByMemberIdAndCouponId(member.getId(), couponId);
    }
}