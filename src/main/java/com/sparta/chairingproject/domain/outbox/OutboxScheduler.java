package com.sparta.chairingproject.domain.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.chairingproject.domain.reservation.service.ReservationNotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

	private final ReservationNotificationService reservationNotificationService;

	@Scheduled(cron = "0 0 0 * * ?")
	public void executeDailySchedule() {
		reservationNotificationService.alertPendingReservations();
	}

}
