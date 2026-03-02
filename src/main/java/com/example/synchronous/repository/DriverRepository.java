package com.example.synchronous.repository;

import com.example.synchronous.entity.Driver;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Repository Pattern
public interface DriverRepository extends JpaRepository<Driver, Long> {

    // JPQL Custom Query
    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.rating >= :rating")
    List<Driver> findAvailableDrivers(@Param("rating") Double rating);

    // Native Query Example
    @Query(value = "SELECT * FROM drivers WHERE available = true ORDER BY rating DESC LIMIT 10",
            nativeQuery = true)
    List<Driver> findTopDriversNative();
}