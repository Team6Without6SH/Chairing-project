package com.sparta.chairingproject.domain.reservation.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Secured("ROLE_USER")
    @PostMapping("/stores/{storeId}/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@PathVariable Long storeId,
                                                                 @Valid @RequestBody CreateReservationRequest req,
                                                                 @AuthenticationPrincipal UserDetailsImpl authUser) {
        return new ResponseEntity<>(
                reservationService.createReservation(storeId, req, authUser),
                HttpStatus.OK);
    }
}