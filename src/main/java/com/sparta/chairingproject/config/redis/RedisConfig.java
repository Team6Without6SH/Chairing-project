package com.sparta.chairingproject.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {

	private final RedisProperties redisProperties;

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	// @Bean
	// public LettuceConnectionFactory redisConnectionFactory() {
	// 	RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
	// 	System.out.println("Redis Host: " + redisHost);
	// 	System.out.println("Redis Port: " + redisPort);
	// 	config.setHostName(redisHost); // 환경 변수에서 호스트 가져오기
	// 	config.setPort(redisPort); // 환경 변수에서 포트 가져오기
	// 	return new LettuceConnectionFactory(config);
	// }

	private static LettuceConnectionFactory lettuceConnectionFactory(RedisProperties redisProperties) {
		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisProperties.getHost(),
			redisProperties.getPort());
		LettuceClientConfiguration lettuceConfig = LettuceClientConfiguration.builder()
			.useSsl()
			.disablePeerVerification()
			.build();

		return new LettuceConnectionFactory(redisConfig, lettuceConfig);
	}

	@Bean
	@Primary
	public RedisConnectionFactory redisConnectionFactory() {
		return lettuceConnectionFactory(redisProperties);
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
