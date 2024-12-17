package com.sparta.chairingproject.config.redis;

import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisCacheEvictSubscriber implements MessageListener {

	private final CacheManager cacheManager;

	@Override
	public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
		String cacheKey = new String(message.getBody());
		System.out.println("cache evict message received:" + cacheKey);

		Objects.requireNonNull(cacheManager.getCache("storeDetails")).evictIfPresent(cacheKey);
		System.out.println("cache evicted:" + cacheKey);
	}
}
