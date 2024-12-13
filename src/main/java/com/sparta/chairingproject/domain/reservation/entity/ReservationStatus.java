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
		switch (targetStatus) {
			case APPROVED:
				if (this != ReservationStatus.PENDING) {
					throw new GlobalException(INVALID_STATUS_TRANSITION);
				}
			case REJECTED:
				if (this != ReservationStatus.PENDING && this != ReservationStatus.APPROVED) {
					throw new GlobalException(CANNOT_REJECT_RESERVATION);
				}

			case CANCELED:
				if (this != ReservationStatus.PENDING) {
					throw new GlobalException(CANCELLATION_NOT_ALLOWED);
				}
				break;

			case IN_PROGRESS:
				if (this != ReservationStatus.APPROVED) {
					throw new GlobalException(INVALID_STATUS_TRANSITION);
				}
				break;

			case COMPLETED:
				if (this != ReservationStatus.IN_PROGRESS) {
					throw new GlobalException(INVALID_STATUS_TRANSITION);
				}
				break;

			case PENDING:
				throw new GlobalException(INVALID_STATUS_TRANSITION);
		}
	}
}
