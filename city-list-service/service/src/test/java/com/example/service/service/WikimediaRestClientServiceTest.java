package com.example.service.service;

import com.example.service.exception.ImageNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.example.service.exception.ImageNotFoundException.IMAGE_NOT_FOUND_EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class WikimediaRestClientServiceTest {

    WikimediaRestClientServiceTest() throws IOException {
    }

    @Mock
    private RestTemplate wikimediaApiRestTemplate;

    @InjectMocks
    private WikimediaRestClientService service;

    private final InputStream resourceAsStream = CityServiceTest.class.getClassLoader().getResourceAsStream("Tallinn.jpg");
    private final MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            MediaType.IMAGE_PNG_VALUE,
            resourceAsStream
    );

    @Test
    void fetchImage_GivenValidUrl_ReturnsData() throws IOException {
        // given
        String url = "https//image.jpg";
        ResponseEntity<byte[]> response = new ResponseEntity<>(image.getBytes(), HttpStatus.OK);
        when(wikimediaApiRestTemplate.getForEntity(url, byte[].class))
                .thenReturn(response);

        // when
        var actualImageResponse = service.fetchImage(url);

        // then
        assertThat(actualImageResponse).isNotEmpty();
    }

    @Test
    void fetchImage_GivenInvalidUrl_ThrowsException() {
        // given
        String url = "https//image.jpg";
        var expectedExceptionMessage = String.format(IMAGE_NOT_FOUND_EXCEPTION_MESSAGE, url);
        when(wikimediaApiRestTemplate.getForEntity(url, byte[].class))
                .thenThrow(new RestClientException("404 Not Found"));

        // when
        var exception = assertThrows(
                ImageNotFoundException.class, () -> service.fetchImage(url)
        );
        var actualExceptionMessage = exception.getMessage();

        // then
        assertEquals(expectedExceptionMessage, actualExceptionMessage);
    }

}
