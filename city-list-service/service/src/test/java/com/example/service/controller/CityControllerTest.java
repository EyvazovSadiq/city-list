package com.example.service.controller;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.dto.ErrorResponse;
import com.example.service.entity.CityEntity;
import com.example.service.exception.CityNotFoundException;
import com.example.service.exception.ImageNotFoundException;
import com.example.service.exception.InvalidPageNumberException;
import com.example.service.repository.CityRepository;
import com.example.service.service.CityService;
import com.example.service.service.DatabaseInitializerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.example.service.exception.CityNotFoundException.CITY_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.example.service.exception.ImageNotFoundException.IMAGE_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.example.service.exception.InvalidPageNumberException.INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE;
import static com.example.service.utils.ValidationMessages.CITY_NAME_NOT_BLANK_VALIDATION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CityController.class)
class CityControllerTest {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityService cityService;

    @MockBean
    private DatabaseInitializerService databaseInitializer;

    private final static String CITY_BASE_PATH = "/city-list";

    private final int validPageNumber = 1;
    private final int invalidPageNumber = -1;
    private final Long cityId = 123L;
    private final String cityName = "Tallinn";
    private final String imagePath = "path/to/tallinn.jpg";

    private final InputStream resourceAsStream = CityControllerTest.class.getClassLoader().getResourceAsStream("Tallinn.jpg");
    private final MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            MediaType.IMAGE_PNG_VALUE,
            resourceAsStream
    );

    private final CityUpdateRequest cityUpdateRequest = new CityUpdateRequest(cityName);
    private final String requestPayload = objectMapper.writeValueAsString(cityUpdateRequest);
    private final MockMultipartFile cityProperties = new MockMultipartFile("cityProperties", "cityProperties", "application/json", requestPayload.getBytes());

    CityControllerTest() throws IOException {
    }

    @Test
    void update_GivenValidRequestBody_ReturnsUpdatedData() throws Exception {
        // given
        var expectedCityResponse = new CityResponse(cityId, cityName, imagePath);
        when(cityService.update(cityId, image, cityUpdateRequest)).thenReturn(expectedCityResponse);

        // when
        var builder = MockMvcRequestBuilders.multipart(new URI(CITY_BASE_PATH + "/update/" + cityId));
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        var requestResult = mockMvc.perform(builder
                .file(image)
                .file(cityProperties)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        requestResult
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        assertBody(requestResult, expectedCityResponse);
    }

    @Test
    void update_GivenNonExistentCityId_ReturnsErrorDto() throws Exception {
        // given
        var errorMessage = String.format(CITY_NOT_FOUND_EXCEPTION_MESSAGE, cityId);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.NOT_FOUND.toString())
                .errorMessages(List.of(errorMessage))
                .build();
        when(cityService.update(cityId, image, cityUpdateRequest)).thenThrow(new CityNotFoundException(cityId));

        // when
        var builder = MockMvcRequestBuilders.multipart(new URI(CITY_BASE_PATH + "/update/" + cityId));
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        var requestResult = mockMvc.perform(builder
                .file(image)
                .file(cityProperties)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        requestResult
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        assertBody(requestResult, expectedErrorResponse);
    }

    @Test
    void update_GivenMissingRequiredFields_ReturnsErrorDto() throws Exception {
        // given
        var errorMessages = List.of(CITY_NAME_NOT_BLANK_VALIDATION_MESSAGE);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.BAD_REQUEST.toString())
                .errorMessages(errorMessages)
                .build();

        // when
        String requestPayload = objectMapper.writeValueAsString(new CityUpdateRequest(null));
        MockMultipartFile cityProperties = new MockMultipartFile(
                "cityProperties",
                "cityProperties",
                "application/json",
                requestPayload.getBytes());

        var builder = MockMvcRequestBuilders.multipart(new URI(CITY_BASE_PATH + "/update/" + cityId));
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        var requestResult = mockMvc.perform(builder
                .file(cityProperties)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        requestResult.andExpect(status().isBadRequest());
        assertBody(requestResult, expectedErrorResponse);
    }

    @Test
    void getImage_GivenValidCityId_ReturnsImage() throws Exception {
        // given
        InputStream resourceAsStream = CityControllerTest.class.getClassLoader().getResourceAsStream("Tallinn.jpg");
        when(cityService.getImageById(cityId)).thenReturn(resourceAsStream);

        // when
        var requestResult = mockMvc.perform(get(new URI(CITY_BASE_PATH + "/images/" + cityId)));

        // then
        var responseBody = requestResult.andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(responseBody).isNotEmpty();
    }

    @Test
    void getImage_GivenNonExistentCityId_ReturnsErrorDto() throws Exception {
        // given
        var errorMessage = String.format(CITY_NOT_FOUND_EXCEPTION_MESSAGE, cityId);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.NOT_FOUND.toString())
                .errorMessages(List.of(errorMessage))
                .build();
        when(cityService.getImageById(cityId)).thenThrow(new CityNotFoundException(cityId));

        // when
        var requestResult = mockMvc.perform(get(new URI(CITY_BASE_PATH + "/images/" + cityId)));

        // then
        requestResult.andExpect(status().isNotFound());
        assertBody(requestResult, expectedErrorResponse);
    }

    @Test
    void getImage_GivenImageNotFound_ReturnsErrorDto() throws Exception {
        // given
        var errorMessage = String.format(IMAGE_NOT_FOUND_EXCEPTION_MESSAGE, imagePath);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.NOT_FOUND.toString())
                .errorMessages(List.of(errorMessage))
                .build();
        when(cityService.getImageById(cityId)).thenThrow(new ImageNotFoundException(imagePath, new IOException()));

        // when
        var requestResult = mockMvc.perform(get(new URI(CITY_BASE_PATH + "/images/" + cityId)));

        // then
        requestResult.andExpect(status().isNotFound());
        assertBody(requestResult, expectedErrorResponse);
    }

    @Test
    void getByPage_GivenValidPageNumber_ReturnsData() throws Exception {
        // given
        var citiesResponse = List.of(new CityResponse(cityId, cityName, imagePath));
        var expectedCitiesPaginationResponse = new CitiesPaginationResponse(citiesResponse, validPageNumber, 5, 25);
        doNothing().when(databaseInitializer).initializeDB();
        when(cityService.getCitiesByPage(validPageNumber)).thenReturn(expectedCitiesPaginationResponse);

        // when
        var requestResult = mockMvc.perform(get(
                new URI(CITY_BASE_PATH + "/get"))
                .param("page", String.valueOf(validPageNumber))
        );

        // then
        verify(databaseInitializer).initializeDB();
        requestResult.andExpect(status().isOk());
        assertBody(requestResult, expectedCitiesPaginationResponse);
    }


    @Test
    void getByPage_GivenInvalidPageNumber_ReturnsErrorDto() throws Exception {
        // given
        var errorMessage = String.format(INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE, invalidPageNumber);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.BAD_REQUEST.toString())
                .errorMessages(List.of(errorMessage))
                .build();
        doNothing().when(databaseInitializer).initializeDB();
        when(cityService.getCitiesByPage(invalidPageNumber)).thenThrow(new InvalidPageNumberException(invalidPageNumber));

        // when
        var requestResult = mockMvc.perform(get(
                new URI(CITY_BASE_PATH + "/get"))
                .param("page", String.valueOf(invalidPageNumber))
        );

        // then
        requestResult.andExpect(status().isBadRequest());
        assertBody(requestResult, expectedErrorResponse);
    }

    @Test
    void getByPageAndName_GivenValidNameAndPageNumber_ReturnsData() throws Exception {
        // given
        var citiesResponse = List.of(new CityResponse(cityId, cityName, imagePath));
        var expectedCitiesPaginationResponse = new CitiesPaginationResponse(citiesResponse, validPageNumber, 5, 25);
        when(cityService.getCitiesByPageAndName(validPageNumber, cityName)).thenReturn(expectedCitiesPaginationResponse);

        // when
        var requestResult = mockMvc.perform(get(
                new URI(CITY_BASE_PATH + "/search"))
                .param("page", String.valueOf(validPageNumber))
                .param("name", cityName)
        );

        // then
        requestResult.andExpect(status().isOk());
        assertBody(requestResult, expectedCitiesPaginationResponse);
    }

    @Test
    void getByPageAndName_GivenInvalidPageNumber_ReturnsErrorDto() throws Exception {
        // given
        var errorMessage = String.format(INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE, invalidPageNumber);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.BAD_REQUEST.toString())
                .errorMessages(List.of(errorMessage))
                .build();
        when(cityService.getCitiesByPageAndName(invalidPageNumber, cityName)).thenThrow(new InvalidPageNumberException(invalidPageNumber));

        // when
        var requestResult = mockMvc.perform(get(
                new URI(CITY_BASE_PATH + "/search"))
                .param("page", String.valueOf(invalidPageNumber))
                .param("name", cityName)
        );

        // then
        requestResult.andExpect(status().isBadRequest());
        assertBody(requestResult, expectedErrorResponse);
    }


    private <T> ObjectAssert<T> assertBody(ResultActions result, T expected) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        //noinspection unchecked
        return (ObjectAssert<T>) assertThat(objectMapper.readValue(body, expected.getClass()))
                .isEqualTo(expected);
    }
}
