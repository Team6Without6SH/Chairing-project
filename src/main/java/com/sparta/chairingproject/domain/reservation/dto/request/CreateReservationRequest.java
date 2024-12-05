package com.sparta.chairingproject.domain.reservation.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.store.entity.Store;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateReservationRequest extends RequestDto {
    private Long storeId;
    private int guestCount;
    private LocalDate date;
    private LocalTime time;

    public Reservation toEntity(Long id, Store store) {
        return Reservation.builder()
                .memberId(id)
                .guestCount(guestCount)
                .date(date)
                .time(time)
                .store(store)
                .build();
    }
}
