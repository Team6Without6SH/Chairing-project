package com.sparta.chairingproject.domain.order.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
	private final WebSocketHandler webSocketHandler;
	private final Set<String> processedMessages = ConcurrentHashMap.newKeySet();

	public void sendNotification(Long memberId, String message) {
		String messageId = extractMessageId(message);
		if (processedMessages.contains(messageId)) {
			System.out.println("중복 메세지 필터링: Message Id: " + messageId);
			return;
		}
		// WebSocket 에 메세지 전송
		webSocketHandler.sendMessageToUser(memberId, message);

		// 메세지 ID 를 Set 에 저장
		processedMessages.add(messageId);
		System.out.println("WebSocket 송신: memberId= " + memberId + ", message= " + message);
	}

	private String extractMessageId(String message) {
		return message.hashCode() + "";
	}
}
