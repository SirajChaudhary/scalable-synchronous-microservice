package com.example.synchronous.dto;

import jakarta.validation.constraints.*;

public record DriverRequest(

        @NotBlank(message = "Driver name is required")
        String name,

        @NotNull(message = "Latitude is required")
        Double latitude,

        @NotNull(message = "Longitude is required")
        Double longitude,

        @DecimalMin(value = "0.0")
        @DecimalMax(value = "5.0")
        Double rating
) {}