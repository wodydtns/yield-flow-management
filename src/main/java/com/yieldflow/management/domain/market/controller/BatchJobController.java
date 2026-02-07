package com.yieldflow.management.domain.market.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yieldflow.management.global.response.ApiResponse;

import java.time.LocalDateTime;

/**
 * 배치 작업 실행 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchJobController {

        private final JobLauncher jobLauncher;
        private final Job marketCodeSyncJob;

        @PostMapping("/market-codes/sync")
        public ResponseEntity<ApiResponse<String>> runMarketCodeSync() {
                try {
                        JobParameters jobParameters = new JobParametersBuilder()
                                        .addString("run.id", LocalDateTime.now().toString())
                                        .toJobParameters();

                        var execution = jobLauncher.run(marketCodeSyncJob, jobParameters);

                        log.info("Market code sync job started. JobExecutionId: {}, Status: {}",
                                        execution.getId(), execution.getStatus());

                        return ResponseEntity.ok(ApiResponse.ok(
                                        "Market code sync job started. JobExecutionId: " + execution.getId()));

                } catch (Exception e) {
                        log.error("Failed to run market code sync job", e);
                        return ResponseEntity.internalServerError()
                                        .body(ApiResponse.<String>builder()
                                                        .error(ApiResponse.Error.of("BATCH_ERROR", e.getMessage()))
                                                        .build());
                }
        }
}
