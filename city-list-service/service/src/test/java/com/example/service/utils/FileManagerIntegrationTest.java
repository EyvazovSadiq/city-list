package com.example.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class FileManagerIntegrationTest {

    private final InputStream resourceAsStream = FileManagerIntegrationTest.class.getClassLoader().getResourceAsStream("Tallinn.jpg");
    private final MockMultipartFile image = new MockMultipartFile(
            "image",
            "image.png",
            MediaType.IMAGE_PNG_VALUE,
            resourceAsStream
    );

    FileManagerIntegrationTest() throws IOException {
    }

    @Test
    void readDataFromCsvFile_ReturnsCityList() throws IOException {
        // when
        var firstElementInCSV = "Tokyo";
        var lastElementInCSV = "Manila";
        List<List<String>> csvFile = FileManager.readDataFromCsvFile();

        // then
        assertThat(csvFile).hasSize(5);
        assertEquals(csvFile.get(0).get(1), firstElementInCSV);
        assertEquals(csvFile.get(4).get(1), lastElementInCSV);
    }

    @Test
    void saveImageToFileStorage_ReturnsPath() throws IOException {
        // when
        var imageName = "Tallinn";
        var imagePath = FileManager.saveImageToFileStorage(image.getBytes(), imageName);

        // then
        assertTrue(imagePath.contains(imageName));

        FileManager.deleteImage(imagePath);
    }

    @Test
    void deleteImage_ReturnTrue() throws IOException {
        // given
        var imagePath = FileManager.saveImageToFileStorage(image.getBytes(), "city_name");

        // when
        var imageDeleted = FileManager.deleteImage(imagePath);

        // then
        assertTrue(imageDeleted);
    }

    @Test
    void deleteImage_NoImage_ReturnFalse() throws IOException {
        // when
        var imageDeleted = FileManager.deleteImage("no_image_path.jpg");

        // then
        assertFalse(imageDeleted);
    }

    @Test
    void loadImageByPath_ReturnsData() throws IOException {
        // given
        String imagePath = FileManager.saveImageToFileStorage(image.getBytes(), image.getName());

        // when
        var actualImage = FileManager.loadImageByPath(imagePath);

        // then
        assertThat(actualImage).isNotEmpty();

        // clean
        FileManager.deleteImage(imagePath);
    }


}
