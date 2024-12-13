package com.sparta.chairingproject.domain.order.subscriber;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderStatusSubscriber {
	private final WebSocketHandler webSocketHandler;

	public OrderStatusSubscriber(WebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

	public void handleMessage(String message) {
		try {
			webSocketHandler.broadcastMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
