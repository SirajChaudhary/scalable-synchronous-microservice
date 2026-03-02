package com.example.synchronous.dto;

public record RideResponse(
        Long rideId,
        Long driverId,
        Double fare,
        String status
) {}