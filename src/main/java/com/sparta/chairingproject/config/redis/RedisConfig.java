package com.sparta.chairingproject.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		Dotenv dotenv = Dotenv.load();

		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		String host = dotenv.get("SPRING_DATA_REDIS_HOST", "localhost");
		System.out.println(host);
		int port = Integer.parseInt(dotenv.get("SPRING_DATA_REDIS_PORT", "6379"));
		System.out.println(port);
		config.setHostName(host); // 환경 변수에서 호스트 가져오기
		config.setPort(port); // 환경 변수에서 포트 가져오기
		return new LettuceConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		return template;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		return container;
	}
}
