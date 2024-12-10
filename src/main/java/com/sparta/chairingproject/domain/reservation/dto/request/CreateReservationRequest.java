package com.sparta.chairingproject.domain.reservation.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
import com.sparta.chairingproject.domain.store.entity.Store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
public class CreateReservationRequest extends RequestDto {

	@NotNull(message = "인원 수는 필수 입력 항목입니다.")
	@Min(value = 1, message = "인원 수는 1명 이상이어야 합니다.")
	private int guestCount;

	@NotEmpty(message = "날짜는 필수 입력 항목입니다.")
	private String date;

	@NotEmpty(message = "시간은 필수 입력 항목입니다.")
	private String time;

	public Reservation toEntity(Long id, Store store) {
		LocalDate ld = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		return Reservation.builder()
			.memberId(id)
			.guestCount(guestCount)
			.date(ld)
			.time(time)
			.status(ReservationStatus.PENDING)
			.store(store)
			.build();
	}
}
