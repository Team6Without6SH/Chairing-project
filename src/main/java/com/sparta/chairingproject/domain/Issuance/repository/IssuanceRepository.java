package com.sparta.chairingproject.domain.Issuance.repository;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssuanceRepository extends JpaRepository<Issuance, Long> {

    Optional<Issuance> findByMemberIdAndCouponId(Long memberId, Long couponId);

    @Query("SELECT i FROM Issuance i WHERE i.member.id = :memberId")
    Page<Issuance> findByMember(@Param("memberId") Long memberId, Pageable pageable);
}
