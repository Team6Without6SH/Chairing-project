package com.sparta.chairingproject.domain.coupon.repository;

import com.sparta.chairingproject.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

}
