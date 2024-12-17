package com.sparta.chairingproject.domain.order.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.sparta.chairingproject.config.redis.RedisSubscriptionEvent;
import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSubscriberService {
	private final ApplicationContext applicationContext;
	private final Map<String, MessageListenerAdapter> subscribers = new ConcurrentHashMap<>();
	private final WebSocketHandler webSocketHandler;

	private RedisMessageListenerContainer getRedisMessageListenerContainer() {
		// RedisMessageListenerContainer를 지연 로드 순환참조 때문에 ㅠ
		return applicationContext.getBean(RedisMessageListenerContainer.class);
	}

	@EventListener
	public void handleSubscriptionEvent(RedisSubscriptionEvent event) {
		subscribeMemberToChannel(event.getMemberId(), event.getChannel(), event.getSession());
	}

	//특정 사용자(memberId)에게 Redis 채널 구독 기능을 추가.
	public void subscribeMemberToChannel(Long memberId, String channel, WebSocketSession session) {
		// Redis 메시지 수신시 처리할 리스너 정의
		MessageListenerAdapter listener = new MessageListenerAdapter((MessageListener)(message, pattern) -> {
			String payload = new String(message.getBody());
			try {
				webSocketHandler.sendMessageToUser(memberId, payload);
				System.out.println("Redis 메시지 전달 성공: memberId=" + memberId + ", message=" + payload);
			} catch (Exception e) {
				System.out.println("WebSocket 메시지 전송 실패: memberId=" + memberId + ", 예외=" + e.getMessage());
			}
		});

		// Redis 채널과 리스너 매핑
		ChannelTopic topic = new ChannelTopic(channel);
		getRedisMessageListenerContainer().addMessageListener(listener, topic);
		subscribers.put(channel, listener);

		System.out.println("Redis 채널 구독 성공: memberId=" + memberId + ", 채널=" + channel);
	}

	//특정 사용자(memberId)의 Redis 채널 구독 해제.
	public void unsubscribeMemberFromChannel(String channel) {
		MessageListenerAdapter listener = subscribers.remove(channel);
		if (listener != null) {
			getRedisMessageListenerContainer().removeMessageListener(listener);
			System.out.println("Redis 채널 구독 해제 성공: 채널=" + channel);
		} else {
			System.out.println("구독 해제 실패: 채널=" + channel + "는 구독 목록에 없음.");
		}
	}

	//Redis 메시지를 직접 처리
	public void handleMessage(String channel, String message) {
		if (channel.startsWith("order-status:member:")) {
			String memberIdStr = channel.split(":")[2];
			Long memberId = Long.valueOf(memberIdStr);
			try {
				webSocketHandler.sendMessageToUser(memberId, message);
				System.out.println("Redis 메시지 전달 성공: memberId=" + memberId + ", message=" + message);
			} catch (Exception e) {
				System.out.println("WebSocket 메시지 전송 실패: memberId=" + memberId + ", 예외=" + e.getMessage());
			}
		} else {
			System.out.println("알 수 없는 채널: " + channel);
		}
	}
}
