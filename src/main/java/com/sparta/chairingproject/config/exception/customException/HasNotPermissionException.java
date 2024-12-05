package com.sparta.chairingproject.config.exception.customException;

import com.sparta.chairingproject.config.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class HasNotPermissionException extends RuntimeException {
	public final ExceptionCode exceptionCode;

	public HasNotPermissionException(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
