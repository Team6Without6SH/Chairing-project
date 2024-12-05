package com.sparta.chairingproject.domain.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
	boolean existsByOwner(Member owner);
}
