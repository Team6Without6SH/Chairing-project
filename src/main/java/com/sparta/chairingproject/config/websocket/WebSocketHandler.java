package com.sparta.chairingproject.config.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {
	private static final ConcurrentHashMap<Long, WebSocketSession> CLIENTS = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		// WebSocket 연결 시 memberId 추출
		Long memberId = extractMemberIdFromSession(session);
		if (memberId != null) {
			addClient(memberId, session);
			System.out.println("WebSocket 연결 성공: memberId=" + memberId + ", 세션 ID=" + session.getId());
		} else {
			System.out.println("WebSocket 연결 실패: memberId가 전달되지 않음");
			try {
				session.close(CloseStatus.BAD_DATA);
			} catch (IOException e) {
				System.out.println("WebSocket 세션 종료 중 오류: " + e.getMessage());
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		// 연결 종료 시 세션 제거
		CLIENTS.entrySet().removeIf(entry -> entry.getValue().equals(session));
		System.out.println("WebSocket 연결 종료: 세션 ID = " + session.getId());
	}

	public void addClient(Long memberId, WebSocketSession session) {
		CLIENTS.put(memberId, session);
		System.out.println("클라이언트 추가 성공: memberId=" + memberId + ", 세션 ID=" + session.getId());
	}

	// 이 메서드는 WebSocket 으로 메세지를 전송
	public void sendMessageToUser(Long memberId, String message) {
		WebSocketSession session = CLIENTS.get(memberId);
		if (session != null && session.isOpen()) {
			try {
				session.sendMessage(new TextMessage(message));
				System.out.println("WebSocket 메시지 전송 성공: 사용자 ID=" + memberId + ", 메시지=" + message);
			} catch (IOException e) {
				System.out.println("WebSocket 메시지 전송 실패: 사용자 ID=" + memberId + ", 예외=" + e.getMessage());
			}
		} else {
			System.out.println("세션 없음: 사용자 ID=" + memberId);
		}
	}

	private Long extractMemberIdFromSession(WebSocketSession session) {
		// WebSocket URL에서 memberId 추출
		Map<String, String> params = parseQueryParams(session.getUri().getQuery());
		if (params.containsKey("memberId")) {
			return Long.valueOf(params.get("memberId"));
		}
		return null;
	}

	private Map<String, String> parseQueryParams(String query) {
		Map<String, String> params = new HashMap<>();
		if (query != null && !query.isEmpty()) {
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				String[] keyValue = pair.split("=");
				if (keyValue.length == 2) {
					params.put(keyValue[0], keyValue[1]);
				}
			}
		}
		return params;
	}
}
