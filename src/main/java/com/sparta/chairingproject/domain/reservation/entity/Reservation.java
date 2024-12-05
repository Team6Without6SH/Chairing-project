package com.sparta.chairingproject.domain.reservation.entity;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.store.entity.Store;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
public class Reservation extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@NotEmpty(message = "회원 아이디는 필수 입력 항목입니다.")
	private Long memberId;

	@Column(nullable = false)
	@NotEmpty(message = "인원 수는 필수 입력 항목입니다.")
	@Min(value = 1, message = "인원 수는 1명 이상이어야 합니다.")
	private int guestCount;

	@Column(nullable = false)
	@NotEmpty(message = "날짜는 필수 입력 항목입니다.")
	private LocalDate date;

	@Column(nullable = false)
	@NotEmpty(message = "시간은 필수 입력 항목입니다.")
	private LocalTime time;

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
				getModifiedAt()
		);
	}
}
