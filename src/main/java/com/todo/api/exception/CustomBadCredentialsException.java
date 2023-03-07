package com.todo.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CustomBadCredentialsException extends RuntimeException {

	public CustomBadCredentialsException(String message) {
		super(message);
	}
}
