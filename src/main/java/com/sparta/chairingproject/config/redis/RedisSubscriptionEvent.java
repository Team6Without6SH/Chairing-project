package com.sparta.chairingproject.config.redis;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;

@Getter
public class RedisSubscriptionEvent extends ApplicationEvent {
	private final Long memberId;
	private final String channel;
	private final WebSocketSession session;

	public RedisSubscriptionEvent(Object source, Long memberId, String channel, WebSocketSession session) {
		super(source);
		this.memberId = memberId;
		this.channel = channel;
		this.session = session;
	}
}
