package com.sparta.chairingproject.domain.reservation.service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;

    /* 일반 사용자 */

    public ReservationResponse createReservation(Long storeId, CreateReservationRequest req, UserDetailsImpl authUser) {
        Long memberId = resolveMember(req, authUser).getId();

        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new GlobalException(ExceptionCode.NOT_FOUND_STORE)
        );

        Reservation savedReservation = reservationRepository.save(req.toEntity(memberId, store));

        return savedReservation.toResponse();
    }

    public ReservationResponse cancelReservation(Long reservationId, RequestDto req, UserDetailsImpl authUser) {
        Long memberId = resolveMember(req, authUser).getId();

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new GlobalException(ExceptionCode.RESERVATION_NOT_FOUND)
        );

        if (!reservation.getMemberId().equals(memberId)){
            throw new GlobalException(ExceptionCode.CANNOT_CANCEL_OTHERS_RESERVATION);
        }

        if (reservation.getStatus() != ReservationStatus.PENDING){
            throw new GlobalException(ExceptionCode.CANCELLATION_NOT_ALLOWED);
        }

        reservation.updateStatus(ReservationStatus.CANCELED);

        return reservationRepository.save(reservation).toResponse();
    }


    /* 사장 */

    public ReservationResponse updateReservation(Long storeId, Long reservationId, @Valid UpdateReservationRequest req, UserDetailsImpl authUser) {
        resolveMember(req, authUser);

        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new GlobalException(ExceptionCode.NOT_FOUND_STORE)
        );

        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new GlobalException(ExceptionCode.RESERVATION_NOT_FOUND)
        );

        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new GlobalException(ExceptionCode.CANNOT_REJECT_RESERVATION);
        }

        ReservationStatus status = ReservationStatus.parse(req.getStatus());

        reservation.updateStatus(status);

        return reservationRepository.save(reservation).toResponse();
    }


    /* 공통 */

    private Member resolveMember(RequestDto req, UserDetailsImpl authUser) {
        if (req.getMemberId() == null || req.getMemberId() == 0) {
            return authUser.getMember();
        } else {
            return memberRepository.findById(req.getMemberId()).orElseThrow(
                    () -> new GlobalException(ExceptionCode.NOT_FOUND_USER)
            );
        }
    }
}
