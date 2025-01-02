package com.sparta.chairingproject.domain.reservation.entity;

import java.time.LocalDate;

import com.sparta.chairingproject.domain.common.entity.OutboxEvent;
import com.sparta.chairingproject.domain.outbox.entity.Outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEvent implements OutboxEvent {

	private OutboxEvent.Type eventType;
	private ReservationType type;
	private Long ownerId;
	private Long memberId;
	private String storeName;
	private LocalDate date;
	private String time;
	private int guestCount;
	private ReservationStatus status;

	public Outbox toOutbox() {
		return type.toOutbox(this);
	}

	@Getter
	enum ReservationType {
		CREATE("예약하기") {
			@Override
			public Outbox toOutbox(ReservationEvent event) {
				return Outbox.builder()
					.eventType(event.eventType)
					.title(event.storeName + "에 새로운 예약 요청")
					.body(
						String.format("예약 정보: %s %s, 인원: %d",
							event.getDate(), event.getTime(), event.getGuestCount()
						))
					.build();
			}
		},
		CANCEL("예약 취소") {
			@Override
			public Outbox toOutbox(ReservationEvent event) {
				return Outbox.builder()
					.eventType(event.eventType)
					.title(event.storeName + "의 예약 취소됨")
					.body(
						String.format("예약 정보: %s %s, 인원: %d",
							event.getDate(), event.getTime(), event.getGuestCount()
						))
					.build();
			}
		},
		UPDATE("예약 상태 변경") {
			@Override
			public Outbox toOutbox(ReservationEvent event) {
				return Outbox.builder()
					.eventType(event.eventType)
					.title(event.storeName + "의 예약이 '" + event.getStatus().getDescription() + "'으로 변경됨")
					.body(
						String.format("예약 정보: %s %s, 인원: %d",
							event.getDate(), event.getTime(), event.getGuestCount()
						))
					.build();
			}
		};

		private final String description;

		public abstract Outbox toOutbox(ReservationEvent event);

		ReservationType(String s) {
			description = s;
		}
	}

}
