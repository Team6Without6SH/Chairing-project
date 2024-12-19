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

	public void subscribeToChannel(Long storeId) {
		String channel = "store-waiting-status:" + storeId;
		MessageListener listener = (message, pattern) -> {
			String payload = new String(message.getBody());
			System.out.println("Redis 메세지 수신: " + payload);

			orderNotificationService.sendNotification(storeId, payload);
		};

		ChannelTopic topic = new ChannelTopic(channel);
		listenerContainer.addMessageListener(listener, topic);
		System.out.println("Redis 채널 구독 성공: channel=" + channel);
	}

	public void subscribeToMemberChannel(Long memberId) {
		String channel = "member-status:" + memberId;
		MessageListener listener = (message, pattern) -> {
			String payload = new String(message.getBody());
			orderNotificationService.sendNotificationToMember(memberId, payload);
		};

		listenerContainer.addMessageListener(listener, new ChannelTopic(channel));
		System.out.println("Redis 채널 구독 성공: channel=" + channel);
	}
}
