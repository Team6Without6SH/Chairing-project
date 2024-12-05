package com.sparta.chairingproject.domain.order.repository;

import com.sparta.chairingproject.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
	int countByStoreIdAndStatus(Long storeId, OrderStatus orderStatus);


    @Query("SELECT o FROM Order o JOIN o.member m JOIN o.menus menu JOIN menu.store s WHERE m.id = :memberId")
    Page<Order> findByMember(@Param("memberId") Long memberId, Pageable pageable);

}
