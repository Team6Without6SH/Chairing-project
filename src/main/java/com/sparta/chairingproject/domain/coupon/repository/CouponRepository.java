package com.sparta.chairingproject.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sparta.chairingproject.domain.coupon.entity.Coupon;

import jakarta.persistence.LockModeType;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
	boolean existsByName(String name);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Coupon c where c.id = :couponId")
	Optional<Coupon> findByIdWithPessimisticLock(@Param("couponId") Long couponId);
}
