package com.sparta.chairingproject.domain.reservation.service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.request.UpdateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;


    public ReservationResponse createReservation(Long storeId, CreateReservationRequest req, UserDetailsImpl authUser) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new GlobalException(ExceptionCode.NOT_FOUND_STORE)
        );

        Long memberId = (req.getMemberId() == null || req.getMemberId() == 0) ? authUser.getMember().getId() : req.getMemberId();

        Reservation savedReservation = reservationRepository.save(req.toEntity(memberId, store));

        return savedReservation.toResponse();
    }

    public ReservationResponse updateReservation(Long storeId, Long reservationId, @Valid UpdateReservationRequest req, UserDetailsImpl authUser) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new GlobalException(ExceptionCode.NOT_FOUND_STORE)
        );

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new GlobalException(ExceptionCode.RESERVATION_NOT_FOUND)
        );


        ReservationStatus status = ReservationStatus.parse(req.getStatus());

        reservation.updateStatus(status);

        return reservationRepository.save(reservation).toResponse();
    }
}
