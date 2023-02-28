package com.example.service.service;

import com.example.service.entity.CityEntity;
import com.example.service.repository.CityRepository;
import com.example.service.utils.FileManager;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@SpringBootTest
@RunWith(SpringRunner.class)
class DatabaseInitializerServiceIntegrationTest {

    DatabaseInitializerServiceIntegrationTest() throws IOException {
    }

    @Autowired
    private DatabaseInitializerService databaseInitializerService;

    @Autowired
    private CityRepository cityRepository;

    @MockBean
    RestTemplate wikimediaApiRestTemplate;

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
    void initDB_GivenNoDataInDB_InitializesDB() throws Exception {
        // given
        assertThat(cityRepository.count()).isZero();
        ResponseEntity<byte[]> response = new ResponseEntity<>(image.getBytes(), HttpStatus.OK);
        when(wikimediaApiRestTemplate.getForEntity("city_image.jpg", byte[].class))
                .thenReturn(response);

        // when
        databaseInitializerService.initializeDB();

        // then
        assertThat(cityRepository.count()).isNotZero();

        // clean
        cityRepository.findAll().forEach(cityEntity -> {
            try {
                FileManager.deleteImage(cityEntity.getImagePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void initDB_GivenSomeDataInDb_DoesNothing() throws Exception {
        // given
        cityRepository.save(new CityEntity(null, "", ""));

        // when
        databaseInitializerService.initializeDB();

        // then
        assertThat(cityRepository.count()).isOne();
    }

}
