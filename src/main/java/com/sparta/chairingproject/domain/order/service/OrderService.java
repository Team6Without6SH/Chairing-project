package com.sparta.chairingproject.domain.order.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.transaction.AfterCommitAction;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.order.dto.request.OrderCancelRequest;
import com.sparta.chairingproject.domain.order.dto.request.OrderRequest;
import com.sparta.chairingproject.domain.order.dto.request.OrderStatusUpdateRequest;
import com.sparta.chairingproject.domain.order.dto.response.OrderCancelResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderPageResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderStatusUpdateResponse;
import com.sparta.chairingproject.domain.order.dto.response.OrderWaitingResponse;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.publisher.OrderStatusPublisher;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final MenuRepository menuRepository;
	private final StoreRepository storeRepository;
	private final OrderStatusPublisher orderStatusPublisher;
	private final RedisSubscriberService redisSubscriberService;
	private final WaitingQueueService waitingQueueService;

	@Transactional
	public OrderResponse createOrder(Long storeId, Member authMember,
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

		// 초기 주문 상태 설정
		int waitingOrders = waitingQueueService.getWaitingQueue(storeId).size();
		int inProgressOrders = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS);
		OrderStatus orderStatus = (waitingOrders == 0 && inProgressOrders < store.getTableCount())
			? OrderStatus.ADMISSION
			: OrderStatus.WAITING;

		Order order = Order.createOf(
			authMember,
			store,
			menus,
			orderStatus,
			totalPrice
		);
		orderRepository.save(order);

		if (orderStatus.equals(OrderStatus.WAITING)) {
			waitingQueueService.addToWaitingQueue(storeId, authMember.getId());
		}

		AfterCommitAction.register(() -> {
			// Redis 채널 구독
			redisSubscriberService.subscribeToChannel(storeId);
			redisSubscriberService.subscribeToMemberChannel(authMember.getId());

			// 개인 메시지 발행 (대기 순서 메시지는 개인 채널로만 전송)
			if (orderStatus == OrderStatus.WAITING) {
				List<String> waitingQueue = waitingQueueService.getWaitingQueue(storeId);
				int userPosition = waitingQueue.indexOf(String.valueOf(authMember.getId())) + 1;
				String personalMessage = "주문이 생성되었습니다. 현재 대기 순서: " + userPosition;
				orderStatusPublisher.publishMemberStatus(authMember.getId(), personalMessage);
			} else {
				String personalMessage = "입장 처리되었습니다.";
				orderStatusPublisher.publishMemberStatus(authMember.getId(), personalMessage);
			}
		});

		int waitingTeams = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.WAITING);

		List<String> menuNames = menus.stream()
			.map(Menu::getName)
			.toList();
		return OrderResponse.builder()
			.orderId(order.getId())
			.orderStatus(order.getStatus().name())
			.menuNames(menuNames)
			.waitingTeams(waitingTeams)
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
		if (!order.getStore().getId().equals(storeId)) {
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

		int inProgressOrders = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS);

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

		if (orderStatus.equals(OrderStatus.WAITING)) {
			waitingQueueService.addToWaitingQueue(storeId, member.getId());
		}

		AfterCommitAction.register(() -> {
			// Redis 채널 구독
			redisSubscriberService.subscribeToChannel(storeId);
			redisSubscriberService.subscribeToMemberChannel(member.getId());

			// 개인 메시지 발행 (대기 순서 메시지는 개인 채널로만 전송)
			if (orderStatus == OrderStatus.WAITING) {
				List<String> waitingQueue = waitingQueueService.getWaitingQueue(storeId);
				int userPosition = waitingQueue.indexOf(String.valueOf(member.getId())) + 1;
				String personalMessage = "주문이 생성되었습니다. 현재 대기 순서: " + userPosition;
				orderStatusPublisher.publishMemberStatus(member.getId(), personalMessage);
			} else {
				String personalMessage = "입장 처리되었습니다.";
				orderStatusPublisher.publishMemberStatus(member.getId(), personalMessage);
			}
		});

		int waitingTeams = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.WAITING);

		return OrderWaitingResponse.builder()
			.orderId(order.getId())
			.orderStatus(order.getStatus())
			.waitingTeams(waitingTeams)
			.build();
	}

	@Transactional
	public OrderStatusUpdateResponse updateOrderStatus(Long storeId, Long orderId, OrderStatusUpdateRequest request,
		Member member) {
		Store store = storeRepository.findById(storeId) //가게 검증
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		if (!store.getOwner().getId().equals(member.getId())) {
			throw new GlobalException(ONLY_OWNER_ALLOWED);
		}
		Order order = orderRepository.findById(orderId) //가게에 있는 주문검증
			.orElseThrow(() -> new GlobalException(NOT_FOUND_ORDER));
		if (!order.getStore().getId().equals(storeId)) {
			throw new GlobalException(NOT_ORDER_THIS_STORE);
		}

		OrderStatus newStatus = OrderStatus.fromString(request.getStatus());
		OrderStatus currentOrderStatus = order.getStatus();
		if (newStatus.equals(OrderStatus.CANCEL_REQUEST)) {
			throw new GlobalException(CANCEL_REQUEST_NOT_ALLOWED_BY_OWNER);
		}
		if (currentOrderStatus.equals(OrderStatus.COMPLETED) || currentOrderStatus.equals(OrderStatus.CANCELLED)) {
			throw new GlobalException(CANNOT_CHANGE_COMPLETED_OR_CANCELLED);
		}
		if (newStatus.equals(OrderStatus.IN_PROGRESS)) {
			int inProgressCount = orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.IN_PROGRESS);
			if (inProgressCount >= store.getTableCount()) {
				throw new GlobalException(TABLE_FULL_CANNOT_SET_IN_PROGRESS);
			}
		}
		if (currentOrderStatus.equals(OrderStatus.WAITING) && !(newStatus.equals(OrderStatus.ADMISSION)
			|| newStatus.equals(OrderStatus.CANCELLED))) {
			throw new GlobalException(CANCELLED_ADMISSION_ALLOWED_FROM_WAITING);
		}
		if (currentOrderStatus.equals(OrderStatus.ADMISSION) && !(newStatus.equals(OrderStatus.IN_PROGRESS)
			|| newStatus.equals(OrderStatus.CANCELLED))) {
			throw new GlobalException(IN_PROGRESS_CANCELLED_ALLOWED_FROM_ADMISSION);
		}
		order.changeStatus(newStatus);
		orderRepository.save(order);

		AfterCommitAction.register(() -> {
			if (newStatus == OrderStatus.ADMISSION || newStatus == OrderStatus.CANCELLED) {
				waitingQueueService.removeFromWaitingQueue(storeId, order.getMember().getId());
				String personalMessage = newStatus == OrderStatus.ADMISSION
					? "입장 처리되었습니다."
					: "주문이 취소되었습니다.";
				orderStatusPublisher.publishMemberStatus(order.getMember().getId(), personalMessage);
			}

			List<String> queue = waitingQueueService.getWaitingQueue(storeId);
			for (int i = 0; i < queue.size(); i++) {
				Long memberId = Long.valueOf(queue.get(i));
				String queueMessage = "현재 대기 순서: " + (i + 1) + ", 총 대기: " + queue.size();
				orderStatusPublisher.publishStoreStatus(storeId, queueMessage);
			}
		});

		return OrderStatusUpdateResponse.builder()
			.orderId(order.getId())
			.orderStatus(order.getStatus().name())
			.build();
	}

	@Transactional(readOnly = true)
	public Page<OrderPageResponse> getOrdersByStore(Long storeId, Pageable pageable, LocalDate startDate,
		LocalDate endDate, int days) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));
		//입력안하면 기간 전체조회
		if (startDate == null && endDate == null) {
			startDate = LocalDate.MIN;
			endDate = LocalDate.MAX;
		}
		if (startDate == null && endDate != null) {
			startDate = endDate.minusDays(days);
		}
		if (endDate == null && startDate != null) {
			endDate = startDate.plusDays(days); // 조회 기간 days 를 기준으로 앞뒤에 기간을 추가해서 조회
		}
		return orderRepository.findByStoreAndCreatedAtBetween(store.getId(), startDate.atStartOfDay(),
				endDate.atTime(23, 59, 59), pageable)
			.map(OrderPageResponse::from);
	}
}
