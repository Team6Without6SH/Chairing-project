package com.sparta.chairingproject.domain.reservation.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateReservationRequestDto {
    private Long storeId;
    private int guestCount;
    private LocalDate date;
    private LocalTime time;

}
