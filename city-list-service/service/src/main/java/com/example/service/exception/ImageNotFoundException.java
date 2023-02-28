package com.example.service.exception;

public class ImageNotFoundException extends RuntimeException {
    public static final String IMAGE_NOT_FOUND_EXCEPTION_MESSAGE = "Image is not found. cause = %s";

    public ImageNotFoundException(String url, Exception ex) {
        super(String.format(IMAGE_NOT_FOUND_EXCEPTION_MESSAGE, url),  ex);
    }
}
