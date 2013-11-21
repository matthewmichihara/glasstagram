package com.fourpool.glasstagram;

public class ErrorEvent {
	private final String error;

	public ErrorEvent(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}
}
