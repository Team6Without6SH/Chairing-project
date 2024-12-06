package com.sparta.chairingproject.domain.reservation.dto.response;

import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResponse {
    private Long id;

    private Long memberId;

    private Long storeId;

    private int guestCount;

    private LocalDate date;

    private String time;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    private ReservationStatus status;
}
