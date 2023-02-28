package com.example.service.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileManager {

    private FileManager() {

    }

    private static final String CITIES_RESOURCE_FILE_NAME = "cities.csv";
    private static final String CITIES_STORAGE_FOLDER = "images/cities";
    private static final String COMMA_DELIMITER = ",";

    public static List<List<String>> readDataFromCsvFile() throws IOException {
        InputStream resourceAsStream = FileManager.class.getClassLoader().getResourceAsStream(CITIES_RESOURCE_FILE_NAME);
        InputStreamReader streamReader = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);

        List<List<String>> cityList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(streamReader)) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                cityList.add(Arrays.asList(values));
            }
        }
        return cityList;
    }

    public static InputStream loadImageByPath(String path) throws IOException {
        File initialFile = new File(path);
        return new FileInputStream(initialFile);
    }

    public static String saveImageToFileStorage(byte[] data, String imageName) {
        var imagePath = String.format("%s/%s_%s.jpg", CITIES_STORAGE_FOLDER, imageName, UUID.randomUUID());
        Path path = Paths.get(imagePath);
        try {
            Files.createDirectories(path.getParent());
            return Files.write(path, data).toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteImage(String filePath) throws IOException {
        Path pathToBeDeleted = Paths.get(filePath);
        return Files.deleteIfExists(pathToBeDeleted);
    }
}
