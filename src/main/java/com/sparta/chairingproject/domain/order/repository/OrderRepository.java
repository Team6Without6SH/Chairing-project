package com.sparta.chairingproject.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
	int countByStoreIdAndStatus(Long storeId, OrderStatus orderStatus);
}
