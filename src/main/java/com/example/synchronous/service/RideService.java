package com.example.synchronous.service;

import com.example.synchronous.dto.RideRequest;
import com.example.synchronous.dto.RideResponse;

import java.util.concurrent.CompletableFuture;

/*
 RideService (Service Pattern)
*/
public interface RideService {

    RideResponse requestRide(RideRequest request);

    CompletableFuture<RideResponse> requestRideAsync(RideRequest request);
}