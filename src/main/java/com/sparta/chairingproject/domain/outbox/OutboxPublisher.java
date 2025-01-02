package com.sparta.chairingproject.domain.outbox;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.domain.fcm.sevice.FcmServiceImpl;
import com.sparta.chairingproject.domain.outbox.entity.Outbox;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

	private final OutboxRepository outboxRepository;
	private final FcmServiceImpl fcmService;

	@Scheduled(fixedDelay = 5000) // 5초 간격으로 실행
	public void processOutboxEvents() {
		List<Outbox> pendingEvents = outboxRepository.findByStatus(Outbox.Status.PENDING);

		for (Outbox event : pendingEvents) {
			event.setStatus(Outbox.Status.PROCESSING);
			try {
				handleEvent(event);
				event.setStatus(Outbox.Status.PUBLISHED);
				event.setPublishedAt(LocalDateTime.now());
				outboxRepository.save(event);
			} catch (Exception e) {
				event.setStatus(Outbox.Status.FAILED);
				// 실패 시 로그 기록 및 알림 재시도 로직
				//log.error("Failed to process outbox event: {}", event.getId(), e);
			}
		}
	}

	private void handleEvent(Outbox outbox) {
		fcmService.sendMessageToUser(
			outbox.getUserId(),
			outbox.getTitle(),
			outbox.getBody()
		);
	}
}