package com.sparta.chairingproject.domain.order.subscriber;

import java.io.IOException;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.sparta.chairingproject.config.websocket.WebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class OrderStatusSubscriber implements MessageListener {
// 	private final WebSocketHandler webSocketHandler;
//
// 	@Override
// 	public void onMessage(Message message, byte[] pattern) {
// 		String channel = new String(pattern);
// 		String payload = new String(message.getBody());
// 		log.info("Redis 메시지 수신: channel={}, payload={}", channel, payload);
//
// 		if (channel.startsWith("order-status:member:")) {
// 			String memberIdStr = channel.split(":")[2];
// 			Long memberId = Long.valueOf(memberIdStr);
// 			try {
// 				webSocketHandler.sendMessageToUser(memberId, payload);
// 			} catch (IOException e) {
// 				log.error("WebSocket 메시지 전송 중 오류 발생", e);
// 			}
// 		} else {
// 			log.warn("알 수 없는 채널: {}", channel);
// 		}
// 	}
// }
