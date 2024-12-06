package com.sparta.chairingproject.domain.order.repository;

import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    int countByStoreIdAndStatus(Long storeId, OrderStatus orderStatus);


    @Query("SELECT o FROM Order o JOIN FETCH o.member m WHERE m.id = :memberId")
    Page<Order> findByMember(@Param("memberId") Long memberId, Pageable pageable);

}
