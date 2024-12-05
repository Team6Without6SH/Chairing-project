package com.sparta.chairingproject.domain.store.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.store.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {

}
