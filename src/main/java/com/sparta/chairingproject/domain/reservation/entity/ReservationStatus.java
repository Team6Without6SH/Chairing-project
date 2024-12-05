package com.sparta.chairingproject.domain.reservation.entity;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;

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
            throw new GlobalException(ExceptionCode.RESERVATION_STATUS_NOT_FOUND);
        }
    }
}
