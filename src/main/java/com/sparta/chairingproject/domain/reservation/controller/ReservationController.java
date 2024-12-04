package com.sparta.chairingproject.domain.reservation.controller;

import com.sparta.chairingproject.domain.reservation.service.ReservationService;
import com.sparta.chairingproject.util.ResponseBodyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ResponseBodyDto<ReservationResponseDto>> createCoupon(@RequestBody ReservationCreateponRequestDto requestDto) {
        return new ResponseEntity<>(
                ResponseBodyDto.success("예약 완료", reservationService.createReservation(requestDto)),
                HttpStatus.OK);
    }
}
