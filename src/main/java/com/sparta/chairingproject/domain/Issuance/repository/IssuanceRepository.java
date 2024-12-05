package com.sparta.chairingproject.domain.Issuance.repository;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssuanceRepository extends JpaRepository<Issuance, Long> {
    Optional<Issuance> findByMemberIdAndCouponId(Long memberId, Long couponId);

    List<Issuance> findAllByMemberId(Long MemberId);
}
