package com.example.synchronous.dto;

import java.time.LocalDateTime;

public record DriverResponse(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        Double rating,
        Boolean available,
        LocalDateTime createdAt
) {}