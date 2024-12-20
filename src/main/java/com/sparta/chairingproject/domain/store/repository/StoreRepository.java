package com.sparta.chairingproject.domain.store.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;

public interface StoreRepository extends JpaRepository<Store, Long> {

	List<Store> findAllByRequestStatusAndStatus(StoreRequestStatus requestStatus, StoreStatus status);

	int countByOwner(Member owner);

	Optional<Store> findByIdAndOwnerId(Long storeId, Long ownerId);

	@Modifying
	@Transactional
	@Query("UPDATE Store s SET s.requestStatus = 'DELETED', s.deletedAt = :deletedAt, s.status = 'CLOSED' WHERE s.id = :storeId")
	void softDeleteById(@Param("storeId") Long storeId, @Param("deletedAt") LocalDateTime deletedAt);

	@Query("""
		    SELECT s
		    FROM Store s
		    LEFT JOIN s.orders o
		    GROUP BY s.id
		    ORDER BY COUNT(o) DESC
		""")
	List<Store> findTopStoresByOrderCount(Pageable pageable);
}
