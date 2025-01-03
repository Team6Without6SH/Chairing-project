package com.sparta.chairingproject.domain.order.entity;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import com.sparta.chairingproject.config.exception.customException.GlobalException;

import lombok.Getter;

@Getter
public enum OrderStatus {
	WAITING("대기 중"),
	ADMISSION("입장"),
	IN_PROGRESS("진행 중"),
	COMPLETED("완료"),
	CANCEL_REQUEST("취소 요청 중"),
	CANCELLED("취소");

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}

	public static OrderStatus fromString(String status) {
		for (OrderStatus orderStatus : values()) {
			if (orderStatus.name().equalsIgnoreCase(status)) {
				return orderStatus;
			}
		}
		throw new GlobalException(NOT_VALID_STATUS_NAME);
	}
}
