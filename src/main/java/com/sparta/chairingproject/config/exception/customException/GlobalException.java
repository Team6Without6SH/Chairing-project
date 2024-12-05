package com.sparta.chairingproject.config.exception.customException;

import com.sparta.chairingproject.config.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
	private final ExceptionCode exceptionCode;

	public GlobalException(ExceptionCode exceptionCode) {
		super(exceptionCode.getMessage());
		this.exceptionCode = exceptionCode;
	}
}
