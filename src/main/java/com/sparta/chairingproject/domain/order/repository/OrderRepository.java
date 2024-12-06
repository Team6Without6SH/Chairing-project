package com.sparta.chairingproject.domain.order.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.store.entity.Store;

public interface OrderRepository extends JpaRepository<Order, Long> {

	int countByStoreIdAndStatus(Long storeId, OrderStatus orderStatus);

	@Query("SELECT o FROM Order o JOIN FETCH o.member m WHERE m.id = :memberId")
	Page<Order> findByMember(@Param("memberId") Long memberId, Pageable pageable);

	@Query("SELECT o FROM Order o WHERE o.store.id = :storeId AND o.createdAt BETWEEN :startDate AND :endDate")
	Page<Order> findByStoreAndCreatedAtBetween(@Param("storeId") Long storeId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate,
		Pageable pageable);

	Optional<Order> findByMemberAndStore(Member member, Store store);

}
