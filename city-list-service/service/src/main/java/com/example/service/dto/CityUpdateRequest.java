package com.example.service.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.service.utils.ValidationMessages.CITY_NAME_NOT_BLANK_VALIDATION_MESSAGE;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CityUpdateRequest {

    @NotBlank(message = CITY_NAME_NOT_BLANK_VALIDATION_MESSAGE)
    private String name;
}
