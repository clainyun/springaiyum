package com.ssafy.yumyum.batch.nutrition;

import java.time.LocalDateTime;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NutritionImportReportTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(NutritionImportReportTasklet.class);

    private final NutritionImportRepository repository;
    private final String sourceName;
    private final String sourcePath;

    public NutritionImportReportTasklet(NutritionImportRepository repository, String sourceName, String sourcePath) {
        this.repository = repository;
        this.sourceName = sourceName;
        this.sourcePath = sourcePath;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        LocalDateTime startedAt = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getStartTime();
        repository.saveReport(jobExecutionId, sourceName, sourcePath, startedAt, LocalDateTime.now());
        NutritionImportSummary summary = repository.summarize(jobExecutionId);
        log.info("nutritionImportJob report jobExecutionId={}, sourceName={}, total={}, success={}, failed={}, ready={}",
                jobExecutionId, sourceName, summary.total(), summary.success(), summary.failed(), summary.ready());
        return RepeatStatus.FINISHED;
    }
}
