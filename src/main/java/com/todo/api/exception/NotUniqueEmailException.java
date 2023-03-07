package com.todo.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class NotUniqueEmailException extends RuntimeException {

	public NotUniqueEmailException(String message) {
		super(message);
	}
}
