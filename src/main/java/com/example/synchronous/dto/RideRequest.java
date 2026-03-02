package com.example.synchronous.dto;

import jakarta.validation.constraints.NotNull;

/*
 DTO Pattern using Java Record
*/
public record RideRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Pickup latitude is required")
        Double pickupLatitude,

        @NotNull(message = "Pickup longitude is required")
        Double pickupLongitude
) {}