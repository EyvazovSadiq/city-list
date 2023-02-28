package com.example.service.service;

import com.example.service.exception.ImageNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikimediaRestClientService {

    private final RestTemplate wikimediaApiRestTemplate;

    public byte[] fetchImage(String imageUrl) {
        try {
            ResponseEntity<byte[]> response = wikimediaApiRestTemplate.getForEntity(imageUrl, byte[].class);
            return response.getBody();
        } catch (RestClientException e) {
            log.info("Could not fetch image with url: {}", imageUrl);
            throw new ImageNotFoundException(imageUrl, e);
        }
    }
}
