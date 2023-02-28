package com.example.service.service;

import com.example.service.entity.CityEntity;
import com.example.service.exception.ImageNotFoundException;
import com.example.service.repository.CityRepository;
import com.example.service.utils.FileManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializerService {

    private static final int CITY_IMPORT_BATCH_SIZE = 50;
    private final CityRepository cityRepository;
    private final WikimediaRestClientService restClient;

    private static boolean dbInitializationTriggered = false;

    public void initializeDB() throws IOException, ExecutionException, InterruptedException {
        if (!dbInitializationTriggered && cityRepository.count() == 0) {
            dbInitializationTriggered = true;
            log.info("DB initialization started.");
            var exportedCityList = FileManager.readDataFromCsvFile();
            var exportedCityListSize = exportedCityList.size();
            var exportedCityListPageNumbers = exportedCityListSize % CITY_IMPORT_BATCH_SIZE == 0 ?
                    (exportedCityListSize / CITY_IMPORT_BATCH_SIZE) : (exportedCityListSize / CITY_IMPORT_BATCH_SIZE + 1);

            List<CompletableFuture<Void>> executableCityImportTasks = new ArrayList<>();
            for (int i = 1; i <= exportedCityListPageNumbers; i++) {
                // get 50 of 1000 Exported Cities
                var citiesSublist = getSubList(exportedCityList, i);
                executableCityImportTasks.add(CompletableFuture.runAsync(() -> saveToDB(citiesSublist)));
            }

            CompletableFuture<Void> allFutures = CompletableFuture
                    .allOf(executableCityImportTasks.toArray(new CompletableFuture[executableCityImportTasks.size()]));

            allFutures.get();
            log.info("DB initialization completed.");
        }
    }

    private void saveToDB(List<List<String>> citySublist) {
        // map
        List<CityEntity> cityEntityList = mapCityListToEntityList(citySublist);

        // save to DB
        List<CityEntity> cityEntities = cityRepository.saveAll(cityEntityList);
        log.info("City list saved: {}", cityEntities.size());
    }

    private List<CityEntity> mapCityListToEntityList(List<List<String>> cityList) {
        List<CityEntity> cityEntities = new ArrayList<>();
        for (List<String> city : cityList) {
            try {
                cityEntities.add(mapCityToEntity(city));
            } catch (ImageNotFoundException e) {
                log.error("Skipping the element. {}", e.getMessage());
            }
        }
        return cityEntities;
    }

    private CityEntity mapCityToEntity(List<String> city) throws ImageNotFoundException {
        byte[] image = restClient.fetchImage(city.get(2));
        var imagePath = FileManager.saveImageToFileStorage(image, city.get(1));

        var cityEntity = new CityEntity();
        cityEntity.setName(city.get(1));
        cityEntity.setImagePath(imagePath);
        return cityEntity;
    }

    private List<List<String>> getSubList(List<List<String>> cities, Integer page) {
        int fromIndex = (page - 1) * CITY_IMPORT_BATCH_SIZE;
        if (cities == null || cities.size() <= fromIndex) {
            return Collections.emptyList();
        }
        return cities.subList(fromIndex, Math.min(fromIndex + CITY_IMPORT_BATCH_SIZE, cities.size()));
    }
}
