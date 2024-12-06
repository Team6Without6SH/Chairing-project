package com.sparta.chairingproject.domain.member.dto.response;

import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import lombok.Getter;

@Getter
public class MemberReservationResponse {

    private final Long id;
    private final String date;
    private final String time;
    private final String name;

    public MemberReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.date = reservation.getDate().toString();
        this.time = reservation.getTime();
        this.name = reservation.getStore().getName();
    }

}
