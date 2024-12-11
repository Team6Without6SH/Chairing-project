package com.sparta.chairingproject.domain.reservation.entity;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import com.sparta.chairingproject.config.exception.customException.GlobalException;

public enum ReservationStatus {
	PENDING("대기 중"),
	APPROVED("승인됨"),
	IN_PROGRESS("식사 중"),
	COMPLETED("완료됨"),
	CANCELED("취소됨"), // 손님이 취소
	REJECTED("거절됨"); // 사장이 거절

	private final String description;

	ReservationStatus(String description) {
		this.description = description;
	}

	public static ReservationStatus parse(String status) {
		try {
			return ReservationStatus.valueOf(status.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new GlobalException(RESERVATION_STATUS_NOT_FOUND);
		}
	}

	public void validateTransition(ReservationStatus targetStatus) {
		switch (this) {
			case PENDING:
				if (targetStatus != APPROVED && targetStatus != REJECTED && targetStatus != CANCELED) {
					throw new GlobalException(INVALID_STATUS_TRANSITION);
				}
				break;

			case APPROVED:
				if (targetStatus != IN_PROGRESS && targetStatus != CANCELED) {
					throw new GlobalException(INVALID_STATUS_TRANSITION);
				}
				break;

			case IN_PROGRESS:
				if (targetStatus != COMPLETED) {
					throw new GlobalException(INVALID_STATUS_TRANSITION);
				}
				break;

			case COMPLETED, CANCELED, REJECTED:
				throw new GlobalException(INVALID_STATUS_TRANSITION);
		}
	}
}
