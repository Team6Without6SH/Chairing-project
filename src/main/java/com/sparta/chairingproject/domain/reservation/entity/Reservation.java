package com.sparta.chairingproject.domain.reservation.entity;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

	public void updateStatus(ReservationStatus status) {
		this.status = status;
	}
}
