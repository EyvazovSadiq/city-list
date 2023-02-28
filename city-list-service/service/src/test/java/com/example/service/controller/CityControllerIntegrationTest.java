package com.example.service.controller;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.dto.ErrorResponse;
import com.example.service.entity.CityEntity;
import com.example.service.exception.ImageNotFoundException;
import com.example.service.repository.CityRepository;
import com.example.service.utils.FileManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CityControllerIntegrationTest {

    CityControllerIntegrationTest() throws IOException {
    }

    private final static String CITY_BASE_PATH = "/city-list";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    private final int validPageNumber = 1;
    private final int invalidPageNumber = -1;
    private final Long cityId = 123L;
    private final String cityName = "Tallinn";
    private final String imagePath = "path/to/Tallinn.jpg";

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

    @BeforeEach
    void setup() {
        cityRepository.deleteAll();
    }


    @Test
    void update_GivenValidData_ReturnsUpdatedData() throws Exception {
        // given
        var currentCityName = "city_name";
        var existentCityEntity = new CityEntity(null, currentCityName, imagePath);
        var cityEntityInDB = cityRepository.save(existentCityEntity);

        var expectedCityResponse = new CityResponse(cityEntityInDB.getId(), cityName, "new_generated_path.jpg");

        // when
        var builder = MockMvcRequestBuilders.multipart(new URI(CITY_BASE_PATH + "/update/" + existentCityEntity.getId()));
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
        var responseBody = requestResult
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        var actualCityResponse = objectMapper.readValue(responseBody, CityResponse.class);
        assertThat(actualCityResponse.getId()).isEqualTo(expectedCityResponse.getId());
        assertThat(actualCityResponse.getName()).isEqualTo(expectedCityResponse.getName());
        assertThat(actualCityResponse.getImagePath()).isNotEqualTo(imagePath);

        //clean
        FileManager.deleteImage(actualCityResponse.getImagePath());
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

        // when
        var builder = MockMvcRequestBuilders.multipart(new URI(CITY_BASE_PATH + "/update/123"));
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        var requestResult = mockMvc.perform(builder
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
                "cityProperties", "cityProperties", "application/json", requestPayload.getBytes());

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
    void getImage_GivenValidDataId_ReturnsData() throws Exception {
        // given
        var imagePath = FileManager.saveImageToFileStorage(image.getBytes(), cityName);
        var cityEntityInDB = cityRepository.save(new CityEntity(null, cityName, imagePath));

        // when
        var requestResult = mockMvc.perform(get(new URI(CITY_BASE_PATH + "/images/" + cityEntityInDB.getId())));

        // then
        requestResult.andExpect(status().isOk());

        //clean
        FileManager.deleteImage(cityEntityInDB.getImagePath());
    }


    @Test
    void getImage_GivenImageNotFound_ReturnsErrorDto() throws Exception {
        // given
        CityEntity cityEntity = cityRepository.save(new CityEntity(null, cityName, imagePath));
        var errorMessage = String.format(IMAGE_NOT_FOUND_EXCEPTION_MESSAGE, imagePath);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.NOT_FOUND.toString())
                .errorMessages(List.of(errorMessage))
                .build();

        // when
        var requestResult = mockMvc.perform(get(new URI(CITY_BASE_PATH + "/images/" + cityEntity.getId())));

        // then
        requestResult.andExpect(status().isNotFound());
        assertBody(requestResult, expectedErrorResponse);
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

        // when
        var requestResult = mockMvc.perform(get(new URI(CITY_BASE_PATH + "/images/" + cityId)));

        // then
        requestResult.andExpect(status().isNotFound());
        assertBody(requestResult, expectedErrorResponse);
    }

    @Test
    void getByPage_GivenValidPageNumber_ReturnsData() throws Exception {
        // given the page size is defined as 12, and there are 2 elements saved in repo
        var cityEntity1 = new CityEntity(null, cityName, imagePath);
        var cityEntity2 = new CityEntity(null, cityName, imagePath);
        var cityEntityList = List.of(cityEntity1, cityEntity2);
        cityRepository.saveAll(cityEntityList);

        var expectedTotalPages = 1;
        var expectedCurrentPage = 1;
        var expectedTotalElements = 2;

        // when
        var requestResult = mockMvc.perform(get(
                new URI(CITY_BASE_PATH + "/get"))
                .param("page", String.valueOf(validPageNumber))
        );

        // then
        String responseBody = requestResult.andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        CitiesPaginationResponse citiesPaginationResponse = objectMapper.readValue(responseBody, CitiesPaginationResponse.class);

        assertThat(citiesPaginationResponse.getCities()).hasSameSizeAs(cityEntityList);
        assertThat(citiesPaginationResponse.getCurrentPage()).isEqualTo(expectedCurrentPage);
        assertThat(citiesPaginationResponse.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(citiesPaginationResponse.getTotalElements()).isEqualTo(expectedTotalElements);
    }

    @Test
    void getByPage_GivenInvalidPageNumber_ReturnsErrorDto() throws Exception {
        // given
        cityRepository.saveAll(List.of(new CityEntity(null, cityName, imagePath)));
        var errorMessage = String.format(INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE, invalidPageNumber);
        var expectedErrorResponse = ErrorResponse
                .builder()
                .type(HttpStatus.BAD_REQUEST.toString())
                .errorMessages(List.of(errorMessage))
                .build();

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
        // given the page size is defined as 12, and there are 2 elements matching the query
        var searchKey = "tallinn";
        var cityEntity1 = new CityEntity(null, "Tallinn", imagePath);
        var cityEntity2 = new CityEntity(null, "ttallinNn", imagePath);
        var cityEntity3 = new CityEntity(null, "city with different name", imagePath);
        var cityEntityList = List.of(cityEntity1, cityEntity2, cityEntity3);
        cityRepository.saveAll(cityEntityList);

        var expectedTotalPages = 1;
        var expectedCurrentPage = 1;
        var expectedTotalElements = 2;

        // when
        var requestResult = mockMvc.perform(get(
                new URI(CITY_BASE_PATH + "/search"))
                .param("page", String.valueOf(validPageNumber))
                .param("name", searchKey)
        );

        // then
        var responseBody = requestResult
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var citiesPaginationResponse = objectMapper.readValue(responseBody, CitiesPaginationResponse.class);

        assertThat(citiesPaginationResponse.getCities()).hasSize(2);
        assertThat(citiesPaginationResponse.getCurrentPage()).isEqualTo(expectedCurrentPage);
        assertThat(citiesPaginationResponse.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(citiesPaginationResponse.getTotalElements()).isEqualTo(expectedTotalElements);
        assertThat(citiesPaginationResponse.getCities().stream().anyMatch(cityResponse -> cityResponse.getName().equals(cityEntity1.getName())))
                .isTrue();
        assertThat(citiesPaginationResponse.getCities().stream().anyMatch(cityResponse -> cityResponse.getName().equals(cityEntity2.getName())))
                .isTrue();
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
