package com.sparta.chairingproject.config.exception.customException;

import com.sparta.chairingproject.config.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class GlobalExceptions extends RuntimeException {
	private final ExceptionCode exceptionCode;

	public GlobalExceptions(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
