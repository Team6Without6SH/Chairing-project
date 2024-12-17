package com.sparta.chairingproject.domain.order.service;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSubscriberService {

	private final RedisMessageListenerContainer listenerContainer;
	private final WebSocketHandler webSocketHandler;

	public void subscribeToChannel(Long memberId) {
		String channel = "order-status:member:" + memberId;
		MessageListener listener = (message, pattern) -> {
			String payload = new String(message.getBody());
			System.out.println("Redis 메세지 수신: " + payload);

			webSocketHandler.sendMessageToUser(memberId, payload);
			System.out.println("WebSocket 송신: memberId= " + memberId + ", message= " + payload);
		};

		ChannelTopic topic = new ChannelTopic(channel);
		listenerContainer.addMessageListener(listener, topic);
		System.out.println("Redis 채널 구독 성공: channel=" + channel);
	}
}
