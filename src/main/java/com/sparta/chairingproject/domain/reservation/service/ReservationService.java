package com.sparta.chairingproject.domain.reservation.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.fcm.sevice.FcmServiceImpl;
import com.sparta.chairingproject.domain.outbox.OutboxRepository;
import com.sparta.chairingproject.domain.outbox.entity.Outbox;
import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.request.UpdateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.reservation.entity.ReservationEvent;
import com.sparta.chairingproject.domain.reservation.entity.ReservationStatus;
import com.sparta.chairingproject.domain.reservation.repository.ReservationRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;
import com.sparta.chairingproject.util.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

	private final FcmServiceImpl fcmService;

	private final ReservationRepository reservationRepository;
	private final StoreRepository storeRepository;
	private final OutboxRepository outboxRepository;

	private final AuthUtils authUtils;

	/* 일반 사용자 */

	@Transactional
	public ReservationResponse createReservation(Long storeId, CreateReservationRequest req, UserDetailsImpl authUser) {
		Long memberId = authUtils.findAuthUser(req, authUser).getId();

		Store store = storeRepository.findById(storeId).orElseThrow(
			() -> new GlobalException(NOT_FOUND_STORE)
		);

		Reservation savedReservation = reservationRepository.save(req.toEntity(memberId, store));

		ReservationEvent event = savedReservation.toEvent();

		Outbox outbox = event.toOutbox();

		outboxRepository.save(outbox);

		return savedReservation.toResponse();
	}

	public ReservationResponse cancelReservation(Long reservationId, RequestDto req, UserDetailsImpl authUser) {
		Long memberId = authUtils.findAuthUser(req, authUser).getId();

		Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
			() -> new GlobalException(RESERVATION_NOT_FOUND)
		);

		if (!reservation.getMemberId().equals(memberId)) {
			throw new GlobalException(CANNOT_CANCEL_OTHERS_RESERVATION);
		}

		reservation.updateStatus(ReservationStatus.CANCELED);

		ReservationEvent event = reservation.toEvent();

		Outbox outbox = event.toOutbox();

		outboxRepository.save(outbox);

		return reservationRepository.save(reservation).toResponse();
	}


	/* 사장 */

	public ReservationResponse updateReservation(Long storeId, Long reservationId, @Valid UpdateReservationRequest req,
		UserDetailsImpl authUser) {

		authUtils.findAuthUser(req, authUser);

		Store store = storeRepository.findById(storeId).orElseThrow(
			() -> new GlobalException(NOT_FOUND_STORE)
		);

		Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
			() -> new GlobalException(RESERVATION_NOT_FOUND)
		);

		ReservationStatus targetStatus = ReservationStatus.parse(req.getStatus());

		if (targetStatus == ReservationStatus.CANCELED) {
			throw new GlobalException(CANNOT_CANCEL_OTHERS_RESERVATION);
		}

		reservation.updateStatus(targetStatus);

		ReservationEvent event = reservation.toEvent();

		Outbox outbox = event.toOutbox();

		outboxRepository.save(outbox);

		return reservationRepository.save(reservation).toResponse();
	}

	public Page<ReservationResponse> getReservationList(Long storeId, int page, int size,
		UserDetailsImpl authUser) {
		Store store = storeRepository.findById(storeId).orElseThrow(
			() -> new GlobalException(NOT_FOUND_STORE)
		);

		//Member member = findAuthUser(req, authUser);
		if (!store.getOwner().getId().equals(authUser.getMember().getId())) {
			throw new GlobalException(UNAUTHORIZED_STORE_ACCESS);
		}

		Pageable pageable = PageRequest.of(page, size);

		return reservationRepository.findByStoreId(storeId, pageable)
			.map(Reservation::toResponse);
	}

}
