package com.example.service.service;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.entity.CityEntity;
import com.example.service.exception.CityNotFoundException;
import com.example.service.exception.InvalidPageNumberException;
import com.example.service.repository.CityRepository;
import com.example.service.utils.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.example.service.exception.CityNotFoundException.CITY_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.example.service.exception.InvalidPageNumberException.INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE;
import static com.example.service.service.CityServiceImpl.PAGE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CityServiceTest {

    CityServiceTest() throws IOException {
    }

    @Mock
    private CityRepository repository;

    @InjectMocks
    private CityServiceImpl cityService;

    private final Long cityId = 123L;
    private final String cityName = "Tallinn";
    private final String picturePath = "path/to/tallinn";

    private final InputStream resourceAsStream = CityServiceTest.class.getClassLoader().getResourceAsStream("Tallinn.jpg");
    private final MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            MediaType.IMAGE_PNG_VALUE,
            resourceAsStream
    );

    @Test
    void update_GivenValidDataWithoutImage_UpdatesTheData() throws IOException {
        // given
        var cityEntityById = new CityEntity(cityId, "old city name", picturePath);
        when(repository.findById(cityId)).thenReturn(Optional.of(cityEntityById));

        var cityUpdateRequest = new CityUpdateRequest(cityName);
        var cityEntity = new CityEntity(cityId, cityName, picturePath);
        when(repository.save(cityEntity)).thenReturn(cityEntity);

        var expectedCityResponse = new CityResponse(cityId, cityName, picturePath);

        // when
        var actualCityResponse = cityService.update(cityId, null, cityUpdateRequest);

        // then
        assertEquals(expectedCityResponse, actualCityResponse);
    }

    @Test
    void update_GivenValidDataWithImage_UpdatesTheData() throws IOException {
        // given
        var cityEntityById = new CityEntity(cityId, "old city name", picturePath);
        when(repository.findById(cityId)).thenReturn(Optional.of(cityEntityById));

        var cityUpdateRequest = new CityUpdateRequest(cityName);
        var cityEntity = new CityEntity(cityId, cityName, picturePath);
        when(repository.save(any())).thenReturn(cityEntity);

        var expectedCityResponse = new CityResponse(cityId, cityName, "Tallinn_");

        // when
        var actualCityResponse = cityService.update(cityId, image, cityUpdateRequest);

        try (MockedStatic<FileManager> mockedFileManager = Mockito.mockStatic(FileManager.class)) {
            mockedFileManager.when(() -> FileManager.saveImageToFileStorage(any(), any()))
                    .thenReturn("new_image_path.jpg");

            assertEquals(expectedCityResponse.getId(), actualCityResponse.getId());
            assertEquals(expectedCityResponse.getName(), actualCityResponse.getName());
        }
    }

    @Test
    void update_GivenNonExistentCityID_ThrowsException() {
        // given
        var cityUpdateRequest = new CityUpdateRequest(cityName);
        var expectedExceptionMessage = String.format(CITY_NOT_FOUND_EXCEPTION_MESSAGE, cityId);
        when(repository.findById(cityId)).thenReturn(Optional.empty());

        // when
        var exception = assertThrows(
                CityNotFoundException.class, () -> cityService.update(cityId, image, cityUpdateRequest)
        );
        var actualExceptionMessage = exception.getMessage();

        // then
        verify(repository).findById(any());
        assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }

    @Test
    void getCitiesByPage_GivenValidPageNumber_ReturnsCityData() {
        // given
        var page = 2;
        var totalElements = 2;
        var pageRequest = PageRequest.of(--page, PAGE_SIZE);

        var cityEntity = new CityEntity(cityId, cityName, picturePath);
        var content = List.of(cityEntity, cityEntity);
        var entityPage = new PageImpl<>(content, pageRequest, totalElements);

        when(repository.findAll(pageRequest)).thenReturn(entityPage);

        var response = new CityResponse(cityId, cityName, picturePath);
        var expectedCitiesResponse = new CitiesPaginationResponse(
                List.of(response, response),
                ++page,
                entityPage.getTotalPages(),
                entityPage.getTotalElements()
        );

        // when
        var actualCitiesResponse = cityService.getCitiesByPage(page);

        // then
        assertEquals(expectedCitiesResponse, actualCitiesResponse);
    }


    @Test
    void getCitiesByPage_GivenInvalidPageNumber_ThrowsException() {
        // given
        var page = -2;
        var expectedExceptionMessage = String.format(INVALID_PAGE_NUMBER_EXCEPTION_MESSAGE, page);

        // when
        var exception = assertThrows(
                InvalidPageNumberException.class, () -> cityService.getCitiesByPage(page)
        );
        var actualExceptionMessage = exception.getMessage();

        // then
        verify(repository, never()).findById(any());
        assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }

    @Test
    void getCitiesByPageAndName_GivenSomeMatchingDataInDB_ReturnsCityData() {
        // given
        var page = 1;
        var pageRequest = PageRequest.of(--page, PAGE_SIZE);
        var content = List.of(new CityEntity(cityId, cityName, picturePath));
        Page<CityEntity> entityPage = new PageImpl<>(content, pageRequest, 1);

        when(repository.findAllByNameIsContainingIgnoreCase(cityName, pageRequest)).thenReturn(entityPage);

        var expectedCitiesResponse = new CitiesPaginationResponse(
                List.of(new CityResponse(cityId, cityName, picturePath)),
                ++page,
                entityPage.getTotalPages(),
                entityPage.getTotalElements()
        );

        // when
        var actualCitiesResponse = cityService.getCitiesByPageAndName(page, cityName);

        // then
        assertEquals(expectedCitiesResponse, actualCitiesResponse);
    }

    @Test
    void getCitiesByPageAndName_GivenNoMatchingDataInDB_ReturnsNoData() {
        // given
        var page = 1;
        var pageRequest = PageRequest.of(page - 1, PAGE_SIZE);
        Page<CityEntity> entityPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

        when(repository.findAllByNameIsContainingIgnoreCase(cityName, pageRequest)).thenReturn(entityPage);

        var expectedCitiesResponse = new CitiesPaginationResponse(
                Collections.emptyList(),
                0,
                entityPage.getTotalPages(),
                entityPage.getTotalElements()
        );

        // when
        var actualCitiesResponse = cityService.getCitiesByPageAndName(page, cityName);

        // then
        assertEquals(expectedCitiesResponse, actualCitiesResponse);
    }

}
