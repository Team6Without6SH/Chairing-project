package com.sparta.chairingproject.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.sparta.chairingproject.domain.coupon.entity.Coupon;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	boolean existsByName(String name);

	@Lock(LockModeType.OPTIMISTIC)
	@Query("select c from Coupon c where c.id = :couponId")
	Optional<Coupon> findByIdWithOptimisticLock(@Param("couponId") Long couponId);
}
