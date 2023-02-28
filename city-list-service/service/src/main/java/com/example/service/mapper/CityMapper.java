package com.example.service.mapper;


import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.entity.CityEntity;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;

public class CityMapper {

    private CityMapper() {
    }

    private static final ModelMapper modelMapper = new ModelMapper();

    public static CityEntity updateRequestToEntity(Long id, String imagePath, CityUpdateRequest cityProperties) {
        var entityNew = new CityEntity();
        entityNew.setId(id);
        entityNew.setImagePath(imagePath);
        entityNew.setName(cityProperties.getName());
        return entityNew;
    }

    public static CityResponse entityToResponse(CityEntity cityEntity) {
        return modelMapper.map(cityEntity, CityResponse.class);
    }

    public static List<CityResponse> entityListToResponseList(List<CityEntity> cityList) {
        return cityList.stream().map(CityMapper::entityToResponse).toList();
    }

    public static CitiesPaginationResponse pageEntityToPageResponse(Page<CityEntity> cityEntityPage) {
        var response = new CitiesPaginationResponse();
        List<CityEntity> cityEntities = cityEntityPage.getContent();

        response.setCities(entityListToResponseList(cityEntities));
        response.setTotalPages(cityEntityPage.getTotalPages());
        response.setTotalElements(cityEntityPage.getTotalElements());
        if (cityEntities.isEmpty()) {
            response.setCurrentPage(0);
        } else {
            response.setCurrentPage(cityEntityPage.getPageable().getPageNumber() + 1);
        }
        return response;
    }
}
