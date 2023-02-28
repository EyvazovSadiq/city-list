package com.example.service.exception;


public class InvalidPageNumberException extends RuntimeException {
    public static final String INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE = "Invalid page number: %s";

    public InvalidPageNumberException(int page) {
        super(String.format(INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE, page));
    }
}
