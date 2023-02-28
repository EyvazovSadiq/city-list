package com.example.service.repository;

import com.example.service.entity.CityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<CityEntity, Long> {

    Page<CityEntity> findAllByNameIsContainingIgnoreCase(String name, Pageable pageable);
}
