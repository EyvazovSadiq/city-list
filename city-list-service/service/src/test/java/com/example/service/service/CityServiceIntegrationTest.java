package com.example.service.service;


import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.entity.CityEntity;
import com.example.service.exception.CityNotFoundException;
import com.example.service.exception.ImageNotFoundException;
import com.example.service.repository.CityRepository;
import com.example.service.utils.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import static com.example.service.exception.CityNotFoundException.CITY_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.example.service.exception.ImageNotFoundException.IMAGE_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.example.service.service.CityServiceImpl.PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RunWith(SpringRunner.class)
class CityServiceIntegrationTest {

    CityServiceIntegrationTest() throws IOException {
    }

    @Autowired
    private CityServiceImpl cityService;

    @Autowired
    private CityRepository cityRepository;

    private final Long cityId = 123L;
    private final String cityName = "Tallinn";
    private final String imagePath = "path/to/tallinn";

    private final InputStream resourceAsStream = CityServiceTest.class.getClassLoader().getResourceAsStream("Tallinn.jpg");
    private final MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            MediaType.IMAGE_PNG_VALUE,
            resourceAsStream
    );

    @BeforeEach
    void setup() {
        cityRepository.deleteAll();
    }


    @Test
    void updateCity_GivenValidDataWithoutImage_UpdatesTheData() throws Exception {
        // given
        var existentCityEntity = cityRepository.save(new CityEntity(null, cityName, imagePath));

        var cityUpdateRequest = new CityUpdateRequest("new city name");
        var expectedCityResponse = new CityResponse(existentCityEntity.getId(), "new city name", imagePath);

        // when
        var actualUpdateResponse = cityService.update(existentCityEntity.getId(), null, cityUpdateRequest);

        // then
        assertThat(actualUpdateResponse).isEqualTo(expectedCityResponse);
    }

    @Test
    void updateCity_GivenValidDataWithImage_UpdatesTheData() throws Exception {
        // given
        var existentCityEntity = cityRepository.save(new CityEntity(null, cityName, imagePath));

        var cityUpdateRequest = new CityUpdateRequest("new city name");
        var expectedCityResponse = new CityResponse(existentCityEntity.getId(), "new city name", "new image path");

        // when
        var actualUpdateResponse = cityService.update(existentCityEntity.getId(), image, cityUpdateRequest);

        // then
        assertThat(actualUpdateResponse.getId()).isEqualTo(expectedCityResponse.getId());
        assertThat(actualUpdateResponse.getName()).isEqualTo(expectedCityResponse.getName());
        assertThat(actualUpdateResponse.getImagePath()).isNotEqualTo(existentCityEntity.getImagePath());
        FileManager.deleteImage(actualUpdateResponse.getImagePath());
    }

    @Test
    void updateCity_GivenNonExistentCityID_ThrowsException() {
        // given
        var cityUpdateRequest = new CityUpdateRequest(cityName);
        var expectedExceptionMessage = String.format(CITY_NOT_FOUND_EXCEPTION_MESSAGE, cityId);

        // when
        var exception = assertThrows(
                CityNotFoundException.class, () -> cityService.update(cityId, null, cityUpdateRequest)
        );
        var actualExceptionMessage = exception.getMessage();

        // then
        assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }


    @Test
    void getImageById_GivenValidCityID_ReturnsImage() throws IOException {
        // given
        String imagePath = FileManager.saveImageToFileStorage(image.getBytes(), cityName);
        CityEntity cityEntity = cityRepository.save(new CityEntity(null, cityName, imagePath));

        // when
        var actualImageResponse = cityService.getImageById(cityEntity.getId());

        // then
        assertThat(actualImageResponse).isNotEmpty();

        // clean
        FileManager.deleteImage(cityEntity.getImagePath());
    }

    @Test
    void getImageById_GivenNonExistentCityID_ThrowsException() {
        // given
        var expectedExceptionMessage = String.format(CITY_NOT_FOUND_EXCEPTION_MESSAGE, cityId);

        // when
        var exception = assertThrows(
                CityNotFoundException.class, () -> cityService.getImageById(cityId)
        );
        var actualExceptionMessage = exception.getMessage();

        // then
        assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }


    @Test
    void getImageById_GivenFileNotFound_ThrowsException() {
        // given
        var cityEntity = cityRepository.save(new CityEntity(null, cityName, imagePath));
        var expectedExceptionMessage = String.format(IMAGE_NOT_FOUND_EXCEPTION_MESSAGE, imagePath);

        // when
        var exception = assertThrows(
                ImageNotFoundException.class, () -> cityService.getImageById(cityEntity.getId())
        );
        var actualExceptionMessage = exception.getMessage();

        // then
        assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }


    @Test
    void getCitiesByPage_GivenValidPageNumber_ReturnsCityData() {
        // given the page size is defined as 12, and there are 2 elements saved in repo
        var cityEntity1 = new CityEntity(null, cityName, imagePath);
        var cityEntity2 = new CityEntity(null, cityName, imagePath);
        var cityEntityList = List.of(cityEntity1, cityEntity2);
        cityRepository.saveAll(cityEntityList);

        var expectedTotalPages = 1;
        var expectedCurrentPage = 1;
        var expectedTotalElements = 2;

        // when
        var actualCitiesResponse = cityService.getCitiesByPage(1);

        // then
        assertThat(actualCitiesResponse.getCities()).hasSameSizeAs(cityEntityList);
        assertThat(actualCitiesResponse.getCurrentPage()).isEqualTo(expectedCurrentPage);
        assertThat(actualCitiesResponse.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(actualCitiesResponse.getTotalElements()).isEqualTo(expectedTotalElements);
    }

    @Test
    void getCitiesByPageAndName_GivenSomeMatchingDataInDB_ReturnsCityData() {
        // given the page size is defined as 12, and there are 2 elements matching the query
        var cityEntity1 = new CityEntity(null, "Tallinn", imagePath);
        var cityEntity2 = new CityEntity(null, "ttallinNn", imagePath);
        var cityEntity3 = new CityEntity(null, "city with different name", imagePath);
        cityRepository.saveAll(List.of(cityEntity1, cityEntity2, cityEntity3));

        var expectedTotalPages = 1;
        var expectedCurrentPage = 1;
        var expectedTotalElements = 2;

        // when
        var actualCitiesResponse = cityService.getCitiesByPageAndName(1, "tallinn");

        // then
        assertThat(actualCitiesResponse.getCurrentPage()).isEqualTo(expectedCurrentPage);
        assertThat(actualCitiesResponse.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(actualCitiesResponse.getTotalElements()).isEqualTo(expectedTotalElements);

        assertThat(actualCitiesResponse.getCities().stream().anyMatch(cityResponse -> cityResponse.getName().equals(cityEntity1.getName())))
                .isTrue();
        assertThat(actualCitiesResponse.getCities().stream().anyMatch(cityResponse -> cityResponse.getName().equals(cityEntity2.getName())))
                .isTrue();
    }

    @Test
    void getCitiesByPageAndName_GivenNoMatchingDataInDB_ReturnsNoData() {
        // given
        var searchKey = "tallinn";
        var pageRequest = PageRequest.of(0, PAGE_SIZE);
        cityRepository.saveAll(List.of(new CityEntity(null, "city with different name", imagePath)));
        var expectedTotalPages = 0;
        var expectedCurrentPage = 0;
        var expectedTotalElements = 0;

        // when
        var actualCitiesResponse = cityService.getCitiesByPageAndName(1, "tallinn");

        // then
        assertThat(actualCitiesResponse.getCities()).isEmpty();
        assertThat(actualCitiesResponse.getCurrentPage()).isEqualTo(expectedCurrentPage);
        assertThat(actualCitiesResponse.getTotalPages()).isEqualTo(expectedTotalPages);
        assertThat(actualCitiesResponse.getTotalElements()).isEqualTo(expectedTotalElements);
    }
}
