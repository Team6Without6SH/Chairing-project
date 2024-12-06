package com.sparta.chairingproject.domain.order.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.CANCELLED_COMPLETED_NOT_ALLOWED;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.NOT_FOUND_ORDER;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.NOT_FOUND_STORE;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.NOT_ORDER_THIS_STORE;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.ONLY_ORDER_ALLOWED;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.PAYED_NOT_EQUAL_BILL;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderCancelRequest;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderCancelResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderWaitingResponse;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public OrderResponse createOrder(Long storeId, UserDetailsImpl authMember,
        OrderRequest orderRequest) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

        List<Menu> menus = Collections.emptyList();
        int totalPrice = 0;
        // 메뉴가 선택된 경우에만 메뉴를 조회 및 가격 계산
        if (orderRequest.getMenuIds() != null && !orderRequest.getMenuIds().isEmpty()) {
            menus = menuRepository.findAllByStoreIdAndMenuIds(storeId, orderRequest.getMenuIds());
            if (menus.size() != orderRequest.getMenuIds().size()) {
                throw new GlobalException(NOT_ORDER_THIS_STORE);
            }

            totalPrice = menus.stream().mapToInt(Menu::getPrice).sum();
            if (totalPrice != orderRequest.getTotalPrice()) {
                throw new GlobalException(PAYED_NOT_EQUAL_BILL);
            }
        }

        OrderStatus orderStatus = OrderStatus.WAITING;

        // 가게에 여유 테이블이 있는 경우 바로 입장 가능하면 ADMISSION 상태로 변경
        int inProgressOrders = orderRepository.countByStoreIdAndStatus(storeId,
            OrderStatus.IN_PROGRESS);
        if (inProgressOrders < store.getTableCount()) {
            orderStatus = OrderStatus.ADMISSION; // 여유 테이블이 있으면 바로 입장
        }

        Order order = Order.createOf(
            authMember.getMember(),
            store,
            menus,
            orderStatus,
            totalPrice
        );
        orderRepository.save(order);

        List<String> menuNames = menus.stream()
            .map(Menu::getName)
            .toList();
        return OrderResponse.builder()
            .orderId(order.getId())
            .orderStatus(order.getStatus().name())
            .menuNames(menuNames)
            .totalPrice(order.getPrice())
            .build();
    }

    @Transactional
    public OrderCancelResponse requestOrderCancellation(Long storeId, Long orderId, Member member,
        OrderCancelRequest memberId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new GlobalException(NOT_FOUND_ORDER));

        if (!order.getMember().getId().equals(member.getId())) {
            throw new GlobalException(ONLY_ORDER_ALLOWED);
        }
        if (order.getMenus().stream().noneMatch(menu -> menu.getStore().getId().equals(storeId))) {
            throw new GlobalException(NOT_ORDER_THIS_STORE);
        }

        if (order.getStatus() == OrderStatus.CANCELLED
            || order.getStatus() == OrderStatus.COMPLETED) {
            throw new GlobalException(CANCELLED_COMPLETED_NOT_ALLOWED);
        }
        order.changeStatus(OrderStatus.CANCEL_REQUEST);

        return OrderCancelResponse.builder()
            .orderId(order.getId())
            .orderStatus(order.getStatus())
            .build();
    }

    @Transactional
    public OrderWaitingResponse createWaiting(Long storeId, Member member, RequestDto request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

        int inProgressOrders = orderRepository.countByStoreIdAndStatus(storeId,
            OrderStatus.IN_PROGRESS);

        // 처음 웨이팅 걸었을때 가게 상황에 맞춰 바로 입장(ADMISSION) or 웨이팅 으로 설정
        OrderStatus orderStatus = (inProgressOrders < store.getTableCount())
            ? OrderStatus.ADMISSION : OrderStatus.WAITING;

        Order order = Order.createOf(
            member,
            store,
            Collections.emptyList(),
            orderStatus,
            0
        );
        orderRepository.save(order);

        int waitingTeams = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.WAITING);

        return OrderWaitingResponse.builder()
            .orderId(order.getId())
            .orderStatus(order.getStatus())
            .waitingTeams(waitingTeams)
            .build();
    }
}
