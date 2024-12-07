package com.sparta.chairingproject.domain.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;

public interface StoreRepository extends JpaRepository<Store, Long> {

	List<Store> findAllByRequestStatusAndStatus(StoreRequestStatus requestStatus, StoreStatus status);

	boolean existsByOwner(Member owner);

	int countByOwner(Member owner);

	Optional<Store> findByIdAndOwnerId(Long storeId, Long ownerId);
}
