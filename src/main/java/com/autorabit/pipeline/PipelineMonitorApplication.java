package com.autorabit.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AutoRABIT Pipeline Monitor — Real-time CI/CD Pipeline Monitoring System
 *
 * Features:
 *  - Real-time pipeline execution tracking via WebSocket (STOMP)
 *  - Live log streaming per pipeline run
 *  - Server-Sent Events (SSE) for live metrics
 *  - JWT-secured REST API
 *  - Pipeline simulation engine with async step execution
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PipelineMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PipelineMonitorApplication.class, args);
    }
}
