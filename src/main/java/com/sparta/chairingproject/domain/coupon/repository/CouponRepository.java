package com.sparta.chairingproject.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.coupon.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	boolean existsByName(String name);
}
