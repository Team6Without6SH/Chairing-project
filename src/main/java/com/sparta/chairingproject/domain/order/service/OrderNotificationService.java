package com.sparta.chairingproject.domain.order.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
	private final WebSocketHandler webSocketHandler;
	// Caffeine Cache 를 사용해서 Message 마다 10 분의 TTL 을 걸기
	private final Cache<String, Boolean> processedMessages = Caffeine.newBuilder()
		.expireAfterWrite(10, TimeUnit.MINUTES)
		.build();
	// 실패 메세지 이곳에 저장
	private final Queue<NotificationMessage> retryQueue = new ConcurrentLinkedQueue<>();

	public void sendNotification(Long memberId, String message) {
		String messageId = extractMessageId(message);
		if (processedMessages.getIfPresent(messageId) != null) {
			System.out.println("중복 메세지 필터링: Message Id: " + messageId);
			return;
		}

		try {
			// WebSocket 에 메세지 전송
			webSocketHandler.sendMessageToUser(memberId, message);

			// 메세지 ID 를 Caffeine Cache 로 각 메세지마다의 TTL 을 걸수있음
			processedMessages.put(messageId, true);
			System.out.println("WebSocket 송신: memberId= " + memberId + ", message= " + message);
		} catch (Exception e) {
			System.out.println("WebSocket 송신 실패, 재시도 큐에 추가: " + message);
			retryQueue.add(new NotificationMessage(memberId, message, messageId));
		}
	}

	@Scheduled(fixedRate = 10000) // 10초마다 메서드 실행
	public void retryFailedMessages() {
		while (!retryQueue.isEmpty()) {
			NotificationMessage message = retryQueue.poll();

			if (processedMessages.getIfPresent(message.getMessageId()) != null) {
				System.out.println("재시도. 중복 메세지 필터링 Message ID: " + message.getMessageId());
				continue;
			}

			try {
				// 메세지 재전송
				webSocketHandler.sendMessageToUser(message.getMemberId(), message.getMessage());

				// 성공한 메세지 저장
				processedMessages.put(message.getMessageId(), true);
				System.out.println("재시도 메세지 전송 성공 Message ID: " + message.getMessageId());
			} catch (Exception e) {
				// 재시도 실패시 큐 에 추가
				System.out.println("재시도 메세지 전송 실패, 다시 큐에 추가합니다.");
				retryQueue.add(message);
			}
		}
	}

	/**
	 * 아래는 내부 편의 메서드
	 */
	private String extractMessageId(String message) {
		return String.valueOf(message.hashCode());
	}

	@RequiredArgsConstructor
	@Getter
	private static class NotificationMessage {
		private final Long memberId;
		private final String message;
		private final String messageId;
	}
}
