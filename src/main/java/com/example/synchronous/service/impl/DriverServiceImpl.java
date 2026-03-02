package com.example.synchronous.service.impl;

import com.example.synchronous.dto.DriverRequest;
import com.example.synchronous.dto.DriverResponse;
import com.example.synchronous.entity.Driver;
import com.example.synchronous.repository.DriverRepository;
import com.example.synchronous.service.DriverService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    /*
    ----------------------------------------------------------------------------
    Create New Driver
    ----------------------------------------------------------------------------
    - Runs inside transaction.
    - Uses blocking JPA safely with Virtual Threads.
    - Sets driver available by default.
    ----------------------------------------------------------------------------
    */
    @Override
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {

        log.info("Creating new driver with name={}", request.name());

        Driver driver = Driver.builder()
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .rating(request.rating())
                .available(true)
                .build();

        Driver savedDriver = driverRepository.save(driver);

        return new DriverResponse(
                savedDriver.getId(),
                savedDriver.getName(),
                savedDriver.getLatitude(),
                savedDriver.getLongitude(),
                savedDriver.getRating(),
                savedDriver.getAvailable(),
                savedDriver.getCreatedAt()
        );
    }
}