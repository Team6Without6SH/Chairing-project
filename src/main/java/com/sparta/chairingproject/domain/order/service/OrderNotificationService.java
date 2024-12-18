package com.sparta.chairingproject.domain.order.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
	private final WebSocketHandler webSocketHandler;
	// Caffeine Cache 를 사용해서 Message 마다 10 분의 TTL 을 걸기
	private final Cache<String, Boolean> processedMessages = Caffeine.newBuilder()
		.expireAfterWrite(10, TimeUnit.MINUTES)
		.build();

	public void sendNotification(Long memberId, String message) {
		String messageId = extractMessageId(message);
		if (processedMessages.getIfPresent(messageId) != null) {
			System.out.println("중복 메세지 필터링: Message Id: " + messageId);
			return;
		}
		// WebSocket 에 메세지 전송
		webSocketHandler.sendMessageToUser(memberId, message);

		// 메세지 ID 를 Caffeine Cache 로 각 메세지마다의 TTL 을 걸수있음
		processedMessages.put(messageId, true);
		System.out.println("WebSocket 송신: memberId= " + memberId + ", message= " + message);
	}

	private String extractMessageId(String message) {
		return message.hashCode() + "";
	}
}
