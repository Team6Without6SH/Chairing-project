package com.sparta.chairingproject.domain.order.publisher;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.domain.order.message.OrderStatusMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStatusPublisher {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public void publishOrderStatus(Long memberId, Long orderId, String message) {
		String channel = "order-status:member:" + memberId;
		try {
			String payload = objectMapper.writeValueAsString(new OrderStatusMessage(orderId, message));
			redisTemplate.convertAndSend(channel, payload);
			System.out.println("Redis 메시지 발행: channel=" + channel + ", message=" + payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("메시지 발행 실패", e);
		}
	}

	public void publishStoreStatus(Long storeId, String message) {
		String channel = "store-waiting-status:" + storeId;
		redisTemplate.convertAndSend(channel, message);
	}

	public void publishMemberStatus(Long memberId, String message) {
		String channel = "member-status:" + memberId;
		redisTemplate.convertAndSend(channel, message);
	}
}
