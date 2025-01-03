package com.sparta.chairingproject.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.domain.common.entity.OutboxEvent;
import com.sparta.chairingproject.domain.outbox.OutboxRepository;
import com.sparta.chairingproject.domain.outbox.entity.Outbox;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationNotificationService {

	private final ReservationRepository reservationRepository;
	private final OutboxRepository outboxRepository;

	@Transactional
	public void alertPendingReservations() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		LocalDateTime startOfDay = yesterday.atStartOfDay();
		LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

		List<Reservation> unapprovedReservations = reservationRepository.findUnapprovedReservations(yesterday);

		for (Reservation reservation : unapprovedReservations) {
			Outbox outbox = Outbox.builder()
				.eventType(OutboxEvent.Type.RESERVATION)
				.userId(reservation.getStore().getOwner().getId())
				.title(reservation.getStore().getName() + "의 예약을 처리하지 않았습니다.")
				.body(
					String.format("예약 정보: %s %s, 인원: %d",
						reservation.getDate(),
						reservation.getTime(),
						reservation.getGuestCount()
					)
				)
				.build();
			outboxRepository.save(outbox);
		}
	}
}