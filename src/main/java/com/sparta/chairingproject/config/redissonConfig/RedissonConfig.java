package com.sparta.chairingproject.config.redissonConfig;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
			.setAddress("redis://localhost:6379")
			.setPassword(null); // 비밀번호가 설정되어 있으면 추가
		return Redisson.create(config);
	}
}
