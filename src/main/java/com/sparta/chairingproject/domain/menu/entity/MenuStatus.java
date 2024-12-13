package com.sparta.chairingproject.domain.menu.entity;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import com.sparta.chairingproject.config.exception.customException.GlobalException;

public enum MenuStatus {
	ACTIVE("판매중"),
	SOLD_OUT("매진"),
	DELETED("삭제됨");

	private final String description;

	MenuStatus(String description) {
		this.description = description;
	}

	public MenuStatus fromString(String status) {
		for (MenuStatus menuStatus : values()) {
			if (menuStatus.name().equalsIgnoreCase(status)) {
				return menuStatus;
			}
		}
		throw new GlobalException(NOT_VALID_STATUS_NAME);
	}
}
