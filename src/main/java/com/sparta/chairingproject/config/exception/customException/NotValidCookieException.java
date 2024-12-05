package com.sparta.chairingproject.config.exception.customException;

import com.sparta.chairingproject.config.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class NotValidCookieException extends Exception {
	private final ExceptionCode exceptionCode;

	public NotValidCookieException(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
