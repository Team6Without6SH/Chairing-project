package com.sparta.chairingproject.domain.order.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {
	private final RedisTemplate<String, String> redisTemplate;

	public void addToWaitingQueue(Long storeId, Long memberId) {
		String queueKey = "store:" + storeId + ":waiting";
		redisTemplate.opsForList().leftPush(queueKey, String.valueOf(memberId));
	}

	public void removeFromWaitingQueue(Long storeId, Long memberId) {
		String queueKey = "store:" + storeId + ":waiting";
		redisTemplate.opsForList().remove(queueKey, 1, String.valueOf(memberId));
	}

	public List<String> getWaitingQueue(Long storeId) {
		String queueKey = "store:" + storeId + ":waiting";
		return redisTemplate.opsForList().range(queueKey, 0, -1);
	}
}
