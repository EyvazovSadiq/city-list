package com.example.service.mapper;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.entity.CityEntity;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class CityMapperTest {

    private final Long cityId = 123L;
    private final String cityName = "Tallinn";
    private final String picturePath = "path/to/tallinn.jpg";

    @Test
    void requestToEntity_GivenUpdateRequest_ReturnsEntity() {
        // given
        var request = new CityUpdateRequest(cityName);
        var expectedEntity = new CityEntity(cityId, cityName, picturePath);

        // when
        var actualEntity = CityMapper.updateRequestToEntity(cityId, picturePath, request);

        // then
        assertEquals(expectedEntity, actualEntity);
    }

    @Test
    void entityToResponse_GivenCityEntity_ReturnsResponse() {
        // given
        var entity = new CityEntity(cityId, cityName, picturePath);
        var expectedResponse = new CityResponse(cityId, cityName, picturePath);

        // when
        var actualResponse = CityMapper.entityToResponse(entity);

        // then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void entityListToResponseList_GivenCityEntityList_ReturnsResponseList() {
        // given
        var entityList = List.of(new CityEntity(cityId, cityName, picturePath));
        var expectedResponse = List.of(new CityResponse(cityId, cityName, picturePath));

        // when
        List<CityResponse> actualResponseList = CityMapper.entityListToResponseList(entityList);

        // then
        assertEquals(expectedResponse, actualResponseList);
    }

    @Test
    void pageEntityToPageResponse_GivenPageEntity_ReturnsResponse() {
        // given
        var page = 1;
        var pageSize = 5;
        var totalElements = 2;
        var cityEntity = new CityEntity(cityId, cityName, picturePath);
        var content = List.of(cityEntity, cityEntity);
        var pageRequest = PageRequest.of(page, pageSize);
        var pageEntity = new PageImpl<>(content, pageRequest, totalElements);

        var response = new CityResponse(cityId, cityName, picturePath);
        var expectedPageResponse = new CitiesPaginationResponse(List.of(response, response),
                ++page,
                pageEntity.getTotalPages(),
                pageEntity.getTotalElements()
        );

        // when
        var actualPageResponse = CityMapper.pageEntityToPageResponse(pageEntity);

        // then
        assertEquals(expectedPageResponse, actualPageResponse);
    }

    @Test
    void pageEntityToPageResponse_GivenPageEntityWithEmptyContent_ReturnsResponseWithPageZero() {
        // given
        var page = 1;
        var pageSize = 5;
        var totalElements = 0;
        var pageRequest = PageRequest.of(page, pageSize);
        List<CityEntity> content = Collections.emptyList();
        var pageEntity = new PageImpl<>(content, pageRequest, totalElements);

        List<CityResponse> response = Collections.emptyList();
        var expectedPageResponse = new CitiesPaginationResponse(response,
                0,
                pageEntity.getTotalPages(),
                pageEntity.getTotalElements()
        );

        // when
        var actualPageResponse = CityMapper.pageEntityToPageResponse(pageEntity);

        // then
        assertEquals(expectedPageResponse, actualPageResponse);
    }
}
