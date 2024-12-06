package com.sparta.chairingproject.domain.reservation.repository;

import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.memberId = :memberId")
    Page<Reservation> findByMember(@Param("memberId") Long memberId, Pageable pageable);
}
