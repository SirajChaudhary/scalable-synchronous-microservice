package com.example.synchronous.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Builder Pattern
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double latitude;

    private Double longitude;

    private Boolean available;

    private Double rating;

    /*
    ----------------------------------------------------------------------------
    Optimistic Locking Version Field
    ----------------------------------------------------------------------------
    - Managed automatically by JPA/Hibernate.
    - Acts as a version counter (0, 1, 2, 3...).
    - Incremented automatically on every update.
    - Prevents concurrent update conflicts.

    Example (generated SQL):
       UPDATE drivers
       SET available = false, version = 1
       WHERE id = 5 AND version = 0;

       → If version is still 0, update succeeds.
       → If another transaction already changed it to 1,
         no rows are updated and OptimisticLockException is thrown.

    - Ensures safe driver allocation under high concurrency.
    ----------------------------------------------------------------------------
    */
    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}