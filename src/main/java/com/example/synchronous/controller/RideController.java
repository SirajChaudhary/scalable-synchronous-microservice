package com.example.synchronous.controller;

import com.example.synchronous.dto.RideRequest;
import com.example.synchronous.dto.RideResponse;
import com.example.synchronous.service.RideService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
@Slf4j
public class RideController {

    private final RideService rideService;

    /*
    ----------------------------------------------------------------------------
    NOTE
    ----------------------------------------------------------------------------
    - Both APIs are designed for the same purpose: booking a ride.
    - Both execute on Virtual Threads (enabled via Spring configuration),
      allowing safe blocking operations without exhausting OS threads.
    - The difference lies in execution strategy:

        1) Synchronous API:
           - Executes all tasks in a single sequential flow.
           - May take slightly longer since operations are performed one after another.

        2) Asynchronous API:
           - Uses CompletableFuture to execute independent tasks in parallel
             (e.g., driver lookup and fare calculation).
           - Improves throughput and performance under high concurrency.

    - Both ultimately persist the ride and return HTTP 201 CREATED.
    ----------------------------------------------------------------------------
    */

    /*
    ----------------------------------------------------------------------------
    Synchronous Ride Booking API
    ----------------------------------------------------------------------------
    - Virtual Threads handle execution (each request runs on its own lightweight virtual thread, allowing safe blocking).
    - Processes request in a single execution flow.
    - Returns HTTP 201 CREATED once processing completes.
    */
    @PostMapping
    public ResponseEntity<RideResponse> requestRide(
            @Valid @RequestBody RideRequest request) {

        log.info("Received synchronous ride request for userId={}", request.userId());

        RideResponse response = rideService.requestRide(request);

        log.info("Synchronous ride processing completed for rideId={}", response.rideId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /*
    ----------------------------------------------------------------------------
    Asynchronous Ride Booking API
    ----------------------------------------------------------------------------
    - Virtual Threads handle execution (each request runs on its own lightweight virtual thread, allowing safe blocking).
    - CompletableFuture enables asynchronous and parallel computation.
        - Allows parallel execution (e.g., driver lookup + fare calculation).
        - Improves throughput under high concurrency.
    - Returns HTTP 201 CREATED once async processing completes.
    */
    @PostMapping("/async")
    public CompletableFuture<ResponseEntity<RideResponse>> requestRideAsync(
            @Valid @RequestBody RideRequest request) {

        log.info("Received asynchronous ride request for userId={}", request.userId());

        return rideService.requestRideAsync(request)
                .thenApply(response -> {
                    log.info("Asynchronous ride processing completed for rideId={}",
                            response.rideId());

                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(response);
                });
    }
}