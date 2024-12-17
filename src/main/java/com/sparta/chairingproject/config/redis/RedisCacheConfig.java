package com.sparta.chairingproject.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisCacheConfig {

	@Primary
	@Bean
	public RedisMessageListenerContainer cacheEvictListenerContainer(
		RedisConnectionFactory redisConnectionFactory,
		MessageListenerAdapter cacheEvictListener) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(cacheEvictListener, new PatternTopic("cacheEvictChannel"));
		return container;
	}

	@Bean
	public MessageListenerAdapter cacheEvictListener(RedisCacheEvictSubscriber subscriber) {
		return new MessageListenerAdapter(subscriber, "onMessage");
	}
}
