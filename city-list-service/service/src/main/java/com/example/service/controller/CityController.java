package com.example.service.controller;

import com.example.service.dto.CitiesPaginationResponse;
import com.example.service.dto.CityResponse;
import com.example.service.dto.CityUpdateRequest;
import com.example.service.service.CityService;
import com.example.service.service.DatabaseInitializerService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/city-list")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class CityController {

    private final DatabaseInitializerService databaseInitializer;
    private final CityService cityService;

    @GetMapping(
            value = "/images/{id}",
            produces = MediaType.IMAGE_JPEG_VALUE
    )
    public @ResponseBody byte[] getImage(@PathVariable(value = "id") Long id) throws IOException {
        return IOUtils.toByteArray(cityService.getImageById(id));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CityResponse> update(
            @PathVariable(name = "id") Long id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart(value = "cityProperties") CityUpdateRequest cityProperties)
            throws IOException {

        return new ResponseEntity<>(cityService.update(id, image, cityProperties), HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<CitiesPaginationResponse> getByPage(@RequestParam(value = "page") int page) throws IOException, ExecutionException, InterruptedException {
        log.info("Got request with page 1");
        databaseInitializer.initializeDB();
        return new ResponseEntity<>(cityService.getCitiesByPage(page), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<CitiesPaginationResponse> getByPageAndName(@RequestParam(value = "page") Integer page,
            @RequestParam(value = "name") String name) {
        return new ResponseEntity<>(cityService.getCitiesByPageAndName(page, name), HttpStatus.OK);
    }
}
