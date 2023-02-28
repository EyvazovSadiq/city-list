package com.example.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CitiesPaginationResponse {
    private List<CityResponse> cities;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
