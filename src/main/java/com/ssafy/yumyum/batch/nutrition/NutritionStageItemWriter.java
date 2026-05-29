package com.ssafy.yumyum.batch.nutrition;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class NutritionStageItemWriter implements ItemWriter<RawNutritionRow>, StepExecutionListener {

    private final NutritionImportRepository repository;
    private long jobExecutionId;

    public NutritionStageItemWriter(NutritionImportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        jobExecutionId = stepExecution.getJobExecutionId();
    }

    @Override
    public void write(Chunk<? extends RawNutritionRow> chunk) {
        repository.insertStagingRows(jobExecutionId, chunk.getItems());
    }
}
