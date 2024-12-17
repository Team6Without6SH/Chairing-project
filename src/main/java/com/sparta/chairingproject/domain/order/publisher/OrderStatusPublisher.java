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

	//특정 사용자(memberId)에게 Redis 채널로 메시지를 발행
	public void publishOrderStatus(Long memberId, Long orderId, String message) {
		// Redis 채널명 생성
		String channel = "order-status:member:" + memberId;

		// 메시지 객체 생성
		OrderStatusMessage orderStatusMessage = new OrderStatusMessage(orderId, message);

		// 메시지를 JSON 문자열로 변환하여 Redis 채널에 발행
		try {
			String jsonMessage = new ObjectMapper().writeValueAsString(orderStatusMessage);
			redisTemplate.convertAndSend(channel, jsonMessage);
			log.info("Redis 메시지 발행 성공: channel={}, message={}", channel, jsonMessage);
		} catch (JsonProcessingException e) {
			log.error("Redis 메시지 발행 실패: JSON 변환 중 오류 발생", e);
		}
	}
}
