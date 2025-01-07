package com.sparta.chairingproject.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Value("${spring.data.redis.port}")
	private int redisPort;

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);
		config.useSingleServer()
			.setAddress(redisAddress)
			.setPassword(null); // 비밀번호가 설정되어 있으면 추가
		return Redisson.create(config);
	}
}
