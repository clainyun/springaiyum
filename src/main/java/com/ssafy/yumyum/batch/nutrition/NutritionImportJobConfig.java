package com.ssafy.yumyum.batch.nutrition;

import java.nio.file.Path;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class NutritionImportJobConfig {

    @Bean
    Job nutritionImportJob(JobRepository jobRepository,
                           Step nutritionStageStep,
                           Step nutritionNormalizeUpsertStep,
                           Step nutritionImportReportStep) {
        return new JobBuilder("nutritionImportJob", jobRepository)
                .start(nutritionStageStep)
                .next(nutritionNormalizeUpsertStep)
                .next(nutritionImportReportStep)
                .build();
    }

    @Bean
    @JobScope
    Step nutritionStageStep(JobRepository jobRepository,
                            PlatformTransactionManager transactionManager,
                            ItemStreamReader<RawNutritionRow> nutritionFileReader,
                            NutritionStageItemWriter nutritionStageItemWriter,
                            @Value("#{jobParameters['chunkSize']}") String chunkSize) {
        return new StepBuilder("nutritionStageStep", jobRepository)
                .<RawNutritionRow, RawNutritionRow>chunk(chunkSize(chunkSize), transactionManager)
                .reader(nutritionFileReader)
                .writer(nutritionStageItemWriter)
                .listener(nutritionStageItemWriter)
                .build();
    }

    @Bean
    @JobScope
    Step nutritionNormalizeUpsertStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      NutritionStagingItemReader nutritionStagingItemReader,
                                      NutritionNormalizeProcessor nutritionNormalizeProcessor,
                                      NutritionProcessItemWriter nutritionProcessItemWriter,
                                      @Value("#{jobParameters['chunkSize']}") String chunkSize) {
        return new StepBuilder("nutritionNormalizeUpsertStep", jobRepository)
                .<NutritionStagingRow, NutritionProcessResult>chunk(chunkSize(chunkSize), transactionManager)
                .reader(nutritionStagingItemReader)
                .processor(nutritionNormalizeProcessor)
                .writer(nutritionProcessItemWriter)
                .listener(nutritionStagingItemReader)
                .build();
    }

    @Bean
    @JobScope
    Step nutritionImportReportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   NutritionImportReportTasklet nutritionImportReportTasklet) {
        return new StepBuilder("nutritionImportReportStep", jobRepository)
                .tasklet(nutritionImportReportTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    ItemStreamReader<RawNutritionRow> nutritionFileReader(
            @Value("#{jobParameters['sourcePath']}") String sourcePath,
            @Value("#{jobParameters['sourceName']}") String sourceName) {
        String name = sourceName(sourcePath, sourceName);
        String lower = sourcePath.toLowerCase();
        if (lower.endsWith(".csv")) {
            return new CsvNutritionItemReader(sourcePath, name);
        }
        if (lower.endsWith(".xlsx")) {
            return new XlsxNutritionItemReader(sourcePath, name);
        }
        throw new IllegalArgumentException("Unsupported file type: " + sourcePath);
    }

    @Bean
    @StepScope
    NutritionStageItemWriter nutritionStageItemWriter(NutritionImportRepository repository) {
        return new NutritionStageItemWriter(repository);
    }

    @Bean
    @StepScope
    NutritionStagingItemReader nutritionStagingItemReader(NutritionImportRepository repository,
                                                          @Value("#{jobParameters['chunkSize']}") String chunkSize) {
        return new NutritionStagingItemReader(repository, chunkSize(chunkSize));
    }

    @Bean
    NutritionNormalizeProcessor nutritionNormalizeProcessor() {
        return new NutritionNormalizeProcessor();
    }

    @Bean
    NutritionProcessItemWriter nutritionProcessItemWriter(NutritionImportRepository repository) {
        return new NutritionProcessItemWriter(repository);
    }

    @Bean
    @StepScope
    NutritionImportReportTasklet nutritionImportReportTasklet(
            NutritionImportRepository repository,
            @Value("#{jobParameters['sourcePath']}") String sourcePath,
            @Value("#{jobParameters['sourceName']}") String sourceName) {
        return new NutritionImportReportTasklet(repository, sourceName(sourcePath, sourceName), sourcePath);
    }

    private static int chunkSize(String value) {
        if (value == null || value.isBlank()) {
            return 100;
        }
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(1, Math.min(parsed, 500));
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    private static String sourceName(String sourcePath, String sourceName) {
        if (sourceName != null && !sourceName.isBlank()) {
            return sourceName;
        }
        return Path.of(sourcePath).getFileName().toString();
    }
}
