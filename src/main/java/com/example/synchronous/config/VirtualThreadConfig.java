package com.example.synchronous.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
 ============================================================================
  Virtual Thread Configuration (Java 21)
 ============================================================================

  Virtual threads are already enabled in application.yml:

      spring:
        threads:
          virtual:
            enabled: true

  This makes Spring MVC handle each HTTP request using Virtual Threads
  instead of traditional platform threads.

  HOWEVER,
  CompletableFuture.supplyAsync() does NOT automatically use Spring’s
  virtual thread executor. By default, it uses ForkJoinPool.commonPool()
  (platform threads).

  Therefore, we define this Executor bean so that async logic like:

      CompletableFuture.supplyAsync(() -> task, executor);

  also runs on Virtual Threads.

  Important:
  - Virtual threads improve application-level concurrency.
  - They do NOT increase database connections.
  - HikariCP pool must still be tuned separately.

 ============================================================================
*/

@Configuration
public class VirtualThreadConfig {

    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {

        /*
         Creates a new Virtual Thread per task.
         Lightweight, scalable, ideal for blocking I/O workloads.
        */
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}