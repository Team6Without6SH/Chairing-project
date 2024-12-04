package com.sparta.chairingproject.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
