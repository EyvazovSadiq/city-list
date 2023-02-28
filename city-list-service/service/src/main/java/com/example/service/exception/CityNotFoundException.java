package com.example.service.exception;


public class CityNotFoundException extends RuntimeException{
    public static final String CITY_NOT_FOUND_EXCEPTION_MESSAGE = "City is not found by id: %s";
    public CityNotFoundException(Long cityId) {
        super(String.format(CITY_NOT_FOUND_EXCEPTION_MESSAGE, cityId));
    }
}
