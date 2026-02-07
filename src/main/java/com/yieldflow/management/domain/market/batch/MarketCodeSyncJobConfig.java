package com.yieldflow.management.domain.market.batch;

import com.yieldflow.management.domain.market.entity.MarketCode;
import com.yieldflow.management.domain.market.repository.MarketCodeRepository;
import com.yieldflow.management.global.external.bithumb.dto.BithumbMarketCodeResponseDto;
import com.yieldflow.management.global.external.bithumb.service.BithumbFeignService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 마켓 코드 동기화 배치 잡
 * 빗썸 API에서 마켓 코드를 조회하여 DB에 동기화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@EnableScheduling
public class MarketCodeSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobLauncher jobLauncher;
    private final BithumbFeignService bithumbFeignService;
    private final MarketCodeRepository marketCodeRepository;

    private static final String JOB_NAME = "marketCodeSyncJob";
    private static final String STEP_NAME = "marketCodeSyncStep";
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job marketCodeSyncJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(marketCodeSyncStep())
                .build();
    }

    @Bean
    public Step marketCodeSyncStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<BithumbMarketCodeResponseDto, MarketCode>chunk(CHUNK_SIZE, transactionManager)
                .reader(marketCodeReader())
                .processor(marketCodeProcessor())
                .writer(marketCodeWriter())
                .build();
    }

    @Bean
    public ItemReader<BithumbMarketCodeResponseDto> marketCodeReader() {
        log.info("Reading market codes from Bithumb API");
        List<BithumbMarketCodeResponseDto> marketCodes = bithumbFeignService.getMarketCodes();
        log.info("Fetched {} market codes from Bithumb API", marketCodes.size());
        return new ListItemReader<>(marketCodes);
    }

    @Bean
    public ItemProcessor<BithumbMarketCodeResponseDto, MarketCode> marketCodeProcessor() {
        return dto -> {
            var existing = marketCodeRepository.findByMarket(dto.market());

            if (existing.isPresent()) {
                var marketCode = existing.get();
                marketCode.updateInfo(dto.koreanName(), dto.englishName(), dto.marketWarning());
                log.debug("Updating market code: {}", dto.market());
                return marketCode;
            } else {
                log.debug("Creating new market code: {}", dto.market());
                return MarketCode.builder()
                        .market(dto.market())
                        .koreanName(dto.koreanName())
                        .englishName(dto.englishName())
                        .marketWarning(dto.marketWarning())
                        .build();
            }
        };
    }

    @Bean
    public ItemWriter<MarketCode> marketCodeWriter() {
        return items -> {
            log.info("Writing {} market codes to database", items.size());
            marketCodeRepository.saveAll(items);
            log.info("Successfully saved market codes");
        };
    }

    @Scheduled(cron = "0 */30 * * * *") // 매 5분마다 (00:00, 00:05, 00:10...)
    public void runMarketCodeSyncJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            var execution = jobLauncher.run(marketCodeSyncJob(), jobParameters);
            log.info("Scheduled market code sync job completed. JobExecutionId: {}, Status: {}",
                    execution.getId(), execution.getStatus());
        } catch (Exception e) {
            log.error("Failed to run scheduled market code sync job", e);
        }
    }
}
