package com.example.service.service;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import org.springframework.web.multipart.MultipartFile;

public interface CityService {

    InputStream getImageById(Long imageName) throws IOException;

    CityResponse update(Long id, MultipartFile image, CityUpdateRequest cityProperties) throws IOException;

    CitiesPaginationResponse getCitiesByPage(int page) throws IOException, ExecutionException, InterruptedException;

    CitiesPaginationResponse getCitiesByPageAndName(Integer page, String name);
}
