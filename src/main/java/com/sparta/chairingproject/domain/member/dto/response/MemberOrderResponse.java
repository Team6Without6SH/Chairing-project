package com.sparta.chairingproject.domain.member.dto.response;


import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import lombok.Getter;

@Getter
public class MemberOrderResponse {

    private final Long id;
    private final OrderStatus status;
    private final int price;
    private final String name;

    public MemberOrderResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.price = order.getPrice();
        this.name = order.getMenus().get(0).getStore().getName();

    }

}
