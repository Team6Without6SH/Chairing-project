package com.sparta.chairingproject.domain.order.service;

import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {
	private final WebSocketHandler webSocketHandler;

	public void sendNotification(Long memberId, String message) {
		webSocketHandler.sendMessageToUser(memberId, message);
		System.out.println("WebSocket 송신: memberId= " + memberId + ", message= " + message);
	}
}
