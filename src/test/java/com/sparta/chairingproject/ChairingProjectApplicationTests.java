package com.sparta.chairingproject;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ChairingProjectApplicationTests {

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void contextLoads() {
		assertNotNull(redisTemplate);
	}

	private void assertNotNull(RedisTemplate<String, String> redisTemplate) {
	}

}
