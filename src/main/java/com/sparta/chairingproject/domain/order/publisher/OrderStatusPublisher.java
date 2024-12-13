package com.sparta.chairingproject.domain.order.publisher;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderStatusPublisher {
	private final RedisTemplate<String, Object> redisTemplate;

	public void publishOrderStatus(String channel, String message) {
		redisTemplate.convertAndSend(channel, message);
	}
}
