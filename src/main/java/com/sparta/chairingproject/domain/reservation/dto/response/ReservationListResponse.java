package com.sparta.chairingproject.domain.reservation.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.sparta.chairingproject.domain.reservation.entity.Reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationListResponse {
	private long totalElements; // 전체 예약 개수
	private int totalPages;     // 전체 페이지 수
	private int currentPage;    // 현재 페이지 번호
	private int pageSize;       // 페이지 크기
	private boolean isFirst;    // 첫 페이지 여부
	private boolean isLast;     // 마지막 페이지 여부

	private List<ReservationResponse> reservationList;

	public ReservationListResponse(Page<Reservation> page) {
		this.totalElements = page.getTotalElements();
		this.totalPages = page.getTotalPages();
		this.currentPage = page.getNumber();
		this.pageSize = page.getSize();
		this.isFirst = page.isFirst();
		this.isLast = page.isLast();
		this.reservationList = page.getContent()
			.stream()
			.map(Reservation::toResponse)
			.toList();
	}
}
