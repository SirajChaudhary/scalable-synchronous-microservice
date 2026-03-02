package com.example.synchronous.service.impl;

import com.example.synchronous.dto.RideRequest;
import com.example.synchronous.dto.RideResponse;
import com.example.synchronous.entity.Driver;
import com.example.synchronous.entity.Ride;
import com.example.synchronous.exception.ResourceNotFoundException;
import com.example.synchronous.repository.DriverRepository;
import com.example.synchronous.repository.RideRepository;
import com.example.synchronous.service.RideService;
import com.example.synchronous.util.FareCalculatorUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceImpl implements RideService {

    private final DriverRepository driverRepository;
    private final RideRepository rideRepository;
    private final Executor virtualThreadExecutor;

    /*
    ----------------------------------------------------------------------------
    Synchronous Ride Booking
    ----------------------------------------------------------------------------
    - Runs inside a single transaction.
    - Uses blocking JPA safely with Virtual Threads.
    - Selects nearest available driver using latitude & longitude.
    ----------------------------------------------------------------------------
    */
    @Override
    @Transactional
    public RideResponse requestRide(RideRequest request) {

        // Fetch available drivers
        List<Driver> drivers = driverRepository.findAvailableDrivers(3.5);

        // Select nearest available driver using Haversine distance
        Driver selectedDriver = drivers.stream()
                .filter(Driver::getAvailable)
                .min(Comparator.comparing(driver ->
                        calculateDistance(
                                request.pickupLatitude(),
                                request.pickupLongitude(),
                                driver.getLatitude(),
                                driver.getLongitude()
                        )))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver", "location", "nearby"));

        // Mark driver unavailable
        selectedDriver.setAvailable(false);
        driverRepository.save(selectedDriver);

        // Calculate fare
        double fare = FareCalculatorUtil.calculateFare(
                request.pickupLatitude(),
                request.pickupLongitude()
        );

        // Persist ride
        Ride ride = Ride.builder()
                .userId(request.userId())
                .driverId(selectedDriver.getId())
                .fare(fare)
                .requestedAt(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        rideRepository.save(ride);

        return new RideResponse(
                ride.getId(),
                selectedDriver.getId(),
                fare,
                "CONFIRMED"
        );
    }

    /*
    ----------------------------------------------------------------------------
    Asynchronous Ride Booking
    ----------------------------------------------------------------------------
    - Uses CompletableFuture for parallel computation.
    - Virtual Threads execute async DB operations.
    - Selects nearest driver using latitude & longitude.
    ----------------------------------------------------------------------------
    */
    @Override
    public CompletableFuture<RideResponse> requestRideAsync(RideRequest request) {

        // Fetch drivers asynchronously
        CompletableFuture<List<Driver>> driversFuture =
                CompletableFuture.supplyAsync(
                        () -> driverRepository.findAvailableDrivers(3.5),
                        virtualThreadExecutor);

        // Calculate fare asynchronously
        CompletableFuture<Double> fareFuture =
                CompletableFuture.supplyAsync(
                        () -> FareCalculatorUtil.calculateFare(
                                request.pickupLatitude(),
                                request.pickupLongitude()),
                        virtualThreadExecutor);

        return driversFuture.thenCombine(fareFuture, (drivers, fare) -> {

            // Select nearest available driver
            Driver selectedDriver = drivers.stream()
                    .filter(Driver::getAvailable)
                    .min(Comparator.comparing(driver ->
                            calculateDistance(
                                    request.pickupLatitude(),
                                    request.pickupLongitude(),
                                    driver.getLatitude(),
                                    driver.getLongitude()
                            )))
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Driver", "location", "nearby"));

            selectedDriver.setAvailable(false);
            driverRepository.save(selectedDriver);

            Ride ride = Ride.builder()
                    .userId(request.userId())
                    .driverId(selectedDriver.getId())
                    .fare(fare)
                    .requestedAt(LocalDateTime.now())
                    .status("CONFIRMED")
                    .build();

            rideRepository.save(ride);

            return new RideResponse(
                    ride.getId(),
                    selectedDriver.getId(),
                    fare,
                    "CONFIRMED"
            );
        });
    }

    /*
     Calculate distance between two geo points using Haversine formula.
    */
    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}