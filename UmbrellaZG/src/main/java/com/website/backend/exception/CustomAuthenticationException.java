package com.website.backend.exception;

public class CustomAuthenticationException extends RuntimeException {

	public CustomAuthenticationException(String message) {
		super(message);
	}

}