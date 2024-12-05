package com.sparta.chairingproject.config.exception.customException;

import com.sparta.chairingproject.config.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class NotValidTokenException extends RuntimeException {
	private final ExceptionCode exceptionCode;

	public NotValidTokenException(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
