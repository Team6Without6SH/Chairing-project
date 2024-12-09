package com.sparta.chairingproject.domain.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface StoreRepository extends JpaRepository<Store, Long> {

	List<Store> findAllByRequestStatusAndStatus(StoreRequestStatus requestStatus, StoreStatus status);

	boolean existsByOwner(Member owner);

	int countByOwner(Member owner);

	Optional<Store> findByIdAndOwnerId(Long storeId, Long ownerId);

	@Modifying
	@Query("UPDATE Store s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :storeId")
	void softDeleteById(@Param("storeId") Long storeId);

}
