package com.sparta.chairingproject.domain.order.service;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSubscriberService {

	private final RedisMessageListenerContainer listenerContainer;
	private final OrderNotificationService orderNotificationService;

	public void subscribeToChannel(Long memberId) {
		String channel = "order-status:member:" + memberId;
		MessageListener listener = (message, pattern) -> {
			String payload = new String(message.getBody());
			System.out.println("Redis 메세지 수신: " + payload);

			orderNotificationService.sendNotification(memberId, payload);
		};

		ChannelTopic topic = new ChannelTopic(channel);
		listenerContainer.addMessageListener(listener, topic);
		System.out.println("Redis 채널 구독 성공: channel=" + channel);
	}
}
