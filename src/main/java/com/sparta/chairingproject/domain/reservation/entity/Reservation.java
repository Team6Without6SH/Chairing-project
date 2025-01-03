package com.sparta.chairingproject.domain.reservation.entity;

import java.time.LocalDate;

import com.sparta.chairingproject.domain.common.entity.OutboxEvent;
import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.store.entity.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "reservations")
public class Reservation extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private int guestCount;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private String time;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	public ReservationResponse toResponse() {
		return new ReservationResponse(
			id,
			memberId,
			store.getId(),
			guestCount,
			date,
			time,
			getCreatedAt(),
			getModifiedAt(),
			status
		);
	}

	public ReservationEvent toEvent(String eventType) {
		return ReservationEvent.builder()
			.eventType(OutboxEvent.Type.RESERVATION)
			.reservationType(ReservationEvent.ReservationType.fromString(eventType))
			.ownerId(getStore().getOwner().getId())
			.memberId(getMemberId())
			.storeName(getStore().getName())
			.date(getDate())
			.time(getTime())
			.guestCount(getGuestCount())
			.build();
	}

	public void updateStatus(ReservationStatus targetStatus) {
		status.validateTransition(targetStatus);
		this.status = targetStatus;
	}

}
