package com.example.service.service;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.entity.CityEntity;
import com.example.service.exception.CityNotFoundException;
import com.example.service.exception.ImageNotFoundException;
import com.example.service.exception.InvalidPageNumberException;
import com.example.service.mapper.CityMapper;
import com.example.service.repository.CityRepository;
import com.example.service.utils.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    public static final int PAGE_SIZE = 12;
    private final CityRepository repository;

    @Override
    public CityResponse update(Long id, MultipartFile image, CityUpdateRequest cityProperties) throws IOException {
        var cityEntityById = repository.findById(id);
        if (cityEntityById.isEmpty()) {
            throw new CityNotFoundException(id);
        }

        String imagePath = cityEntityById.get().getImagePath();
        if (image != null && !image.isEmpty()) {
            imagePath = FileManager.saveImageToFileStorage(image.getBytes(), cityProperties.getName());
            FileManager.deleteImage(cityEntityById.get().getImagePath());
        } else {
            log.warn("New image body was empty. id = {}", id);
        }

        var cityEntity = CityMapper.updateRequestToEntity(id, imagePath, cityProperties);
        var cityResponse = CityMapper.entityToResponse(repository.save(cityEntity));
        log.info("Updated the city by id = {}", cityResponse.getId());
        return cityResponse;
    }

    @Override
    public InputStream getImageById(Long id) {
        Optional<CityEntity> city = repository.findById(id);
        if (city.isEmpty()) {
            throw new CityNotFoundException(id);
        }

        try {
            return FileManager.loadImageByPath(city.get().getImagePath());
        } catch (IOException ex) {
            throw new ImageNotFoundException(city.get().getImagePath(), ex);
        }
    }

    @Override
    public CitiesPaginationResponse getCitiesByPage(int page) {
        var pageRequest = generatePageRequest(page);
        var cityPageEntity = repository.findAll(pageRequest);
        var pageResponse = CityMapper.pageEntityToPageResponse(cityPageEntity);
        log.info("Retrieved cities by page = {}. resultSize = {}", page, pageResponse.getCities().size());
        return pageResponse;
    }


    @Override
    public CitiesPaginationResponse getCitiesByPageAndName(Integer page, String name) {
        var pageRequest = generatePageRequest(page);
        var cityPageEntity = repository.findAllByNameIsContainingIgnoreCase(name, pageRequest);
        var pageResponse = CityMapper.pageEntityToPageResponse(cityPageEntity);
        log.info("Retrieved cities by page = {} and name = {}. resultSize = {}", page, name, pageResponse.getCities().size());
        return pageResponse;
    }

    private PageRequest generatePageRequest(int page) {
        if (page < 1) {
            throw new InvalidPageNumberException(page);
        } else {
            return PageRequest.of(--page, PAGE_SIZE);
        }
    }
}
