package com.sparta.chairingproject.domain.reservation.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.reservation.dto.request.CreateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.request.UpdateReservationRequest;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationListResponse;
import com.sparta.chairingproject.domain.reservation.dto.response.ReservationResponse;
import com.sparta.chairingproject.domain.reservation.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;

	/* 일반 사용자 */

	@Secured("ROLE_USER")
	@PostMapping("/stores/{storeId}/reservations")
	public ResponseEntity<ReservationResponse> createReservation(
		@PathVariable Long storeId,
		@Valid @RequestBody CreateReservationRequest req,
		@AuthenticationPrincipal UserDetailsImpl authUser) {
		return new ResponseEntity<>(
			reservationService.createReservation(storeId, req, authUser),
			HttpStatus.OK);
	}

	@Secured("ROLE_USER")
	@DeleteMapping("/members/reservations/{reservationId}")
	public ResponseEntity<ReservationResponse> cancelReservation(
		@PathVariable Long reservationId,
		@RequestBody RequestDto req,
		@AuthenticationPrincipal UserDetailsImpl authUser) {
		return new ResponseEntity<>(
			reservationService.cancelReservation(reservationId, req, authUser),
			HttpStatus.OK);
	}


	/* 사장 */

	@Secured("ROLE_OWNER")
	@PatchMapping("/stores/{storeId}/reservations/{reservationId}")
	public ResponseEntity<ReservationResponse> updateReservation(
		@PathVariable Long storeId,
		@PathVariable Long reservationId,
		@Valid @RequestBody UpdateReservationRequest req,
		@AuthenticationPrincipal UserDetailsImpl authUser) {
		return new ResponseEntity<>(
			reservationService.updateReservation(storeId, reservationId, req, authUser),
			HttpStatus.OK);
	}

	@Secured("ROLE_OWNER")
	@GetMapping("/stores/{storeId}/reservations")
	public ResponseEntity<ReservationListResponse> getReservationList(
		@PathVariable Long storeId,
		@RequestParam(defaultValue = "0") int page, // 페이지 번호
		@RequestParam(defaultValue = "10") int size, // 페이지 크기
		//@RequestBody RequestDto req,
		@AuthenticationPrincipal UserDetailsImpl authUser
	) {
		return new ResponseEntity<>(
			reservationService.getReservationList(storeId, page, size, authUser),
			HttpStatus.OK);
	}
}
