package com.sparta.chairingproject.config.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheEvictionPublisher {

	private final RedisTemplate<String, Object> redisTemplate;

	public void publish(String cacheKey) {
		redisTemplate.convertAndSend("cacheEvictChannel", cacheKey);
		System.out.println("cacheEvictionPublisher published cacheKey: " + cacheKey);
	}

}
