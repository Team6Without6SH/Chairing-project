package com.sparta.chairingproject.config.redis;

import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisInitializer {
	private final RedisTemplate<String, String> redisTemplate;

	@EventListener(ApplicationReadyEvent.class)
	public void clearRedisData() {
		Set<String> keys = redisTemplate.keys("store:*");
		if (keys != null) {
			redisTemplate.delete(keys);
		}
		System.out.println("Redis 대기열 초기화 완료");
	}
}
