package com.example.service.repository;

import com.example.service.entity.CityEntity;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static com.example.service.service.CityServiceImpl.PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CityRepositoryIntegrationTest {

    @Autowired
    private CityRepository cityRepository;

    private final Long cityId = 123L;
    private final String cityName = "Tallinn";
    private final String picturePath = "path/to/tallinn";

    @BeforeEach
    void setup() {
        cityRepository.deleteAll();
    }

    @Test
    void saveAll_GivenValidCityEntities_SavesData() {
        // given
        var entity1 = new CityEntity(null, cityName, picturePath);
        var entity2 = new CityEntity(null, cityName, picturePath);
        var cityEntityList = List.of(entity1, entity2);

        // when
        cityRepository.saveAll(cityEntityList);

        // then
        assertThat(cityRepository.count()).isEqualTo(cityEntityList.size());
    }

    @Test
    void findById_GivenExistentEntityId_ReturnsEntity() {
        // given
        var existentCityEntity = new CityEntity(null, cityName, picturePath);
        var savedCityEntity = cityRepository.save(existentCityEntity);

        // when
        var cityEntityById = cityRepository.findById(savedCityEntity.getId());

        // then
        assertThat(cityEntityById).contains(savedCityEntity);
    }

    @Test
    void findById_GivenNonExistentEntityId_ReturnsEmpty() {
        // when
        var cityEntityById = cityRepository.findById(cityId);

        // then
        assertThat(cityEntityById).isEmpty();
    }

    @Test
    void save_GivenValidCityEntity_SavesData() {
        // given
        var entity = new CityEntity(null, cityName, picturePath);

        // when
        var savedEntity = cityRepository.save(entity);

        // then
        assertThat(cityRepository.findById(savedEntity.getId())).contains(savedEntity);
    }

    @Test
    void findAll_GivenThereIsDataInDB_ReturnsEntityPageWithData() {
        // given
        var pageRequest = PageRequest.of(0, PAGE_SIZE);
        var entity1 = new CityEntity(null, cityName, picturePath);
        var entity2 = new CityEntity(null, cityName, picturePath);
        var cityEntityList = List.of(entity1, entity2);
        cityRepository.saveAll(cityEntityList);
        var expectedCityEntityPage = new PageImpl<>(cityEntityList, pageRequest, cityEntityList.size());

        // when
        var actualCityEntityPage = cityRepository.findAll(pageRequest);

        // then
        assertThat(actualCityEntityPage).isEqualTo(expectedCityEntityPage);
    }

    @Test
    void findAll_GivenThereIsNoDataInDB_ReturnsEntityPageWithNoData() {
        // given
        var pageRequest = PageRequest.of(0, PAGE_SIZE);
        var expectedCityEntityPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

        // when
        var actualCityEntityPage = cityRepository.findAll(pageRequest);

        // then
        assertThat(actualCityEntityPage).isEqualTo(expectedCityEntityPage);
    }

    @Test
    void findAllByName_GivenSomeMatchingDataInDB_ReturnsEntityPageWithData() {
        // given
        var searchKey = "tallinn";
        var pageRequest = PageRequest.of(0, 12);
        var cityEntity1 = new CityEntity(null, "Tallinn", picturePath);
        var cityEntity2 = new CityEntity(null, "ttallinNn", picturePath);
        var cityEntity3 = new CityEntity(null, "city with different name", picturePath);
        var cityEntityList = List.of(cityEntity1, cityEntity2, cityEntity3);
        cityRepository.saveAll(cityEntityList);

        var expectedCityEntityPage = new PageImpl<>(
                List.of(cityEntity1, cityEntity2), pageRequest, 2);

        // when
        var actualCityEntityPage = cityRepository.
                findAllByNameIsContainingIgnoreCase(searchKey, pageRequest);

        // then
        assertThat(actualCityEntityPage).isEqualTo(expectedCityEntityPage);
    }

    @Test
    void findAllByName_GivenNoMatchingDataInDB_ReturnsEntityPageWithNoData() {
        // given
        var searchKey = "tallinn";
        var pageRequest = PageRequest.of(0, 12);
        var cityEntityList = List.of(new CityEntity(null, "city with different name", picturePath));
        cityRepository.saveAll(cityEntityList);

        var expectedCityEntityPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

        // when
        var actualCityEntityPage = cityRepository.
                findAllByNameIsContainingIgnoreCase(searchKey, pageRequest);

        // then
        assertThat(actualCityEntityPage).isEqualTo(expectedCityEntityPage);
    }


}
