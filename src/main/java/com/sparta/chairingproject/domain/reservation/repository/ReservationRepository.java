package com.sparta.chairingproject.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sparta.chairingproject.domain.reservation.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@Query("SELECT r FROM Reservation r WHERE r.memberId = :memberId")
	Page<Reservation> findByMember(@Param("memberId") Long memberId, Pageable pageable);

	Page<Reservation> findByStoreId(Long storeId, Pageable pageable);

	@Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.createdAt <= :yesterday")
	List<Reservation> findUnapprovedReservations(@Param("yesterday") LocalDateTime yesterday);
}
