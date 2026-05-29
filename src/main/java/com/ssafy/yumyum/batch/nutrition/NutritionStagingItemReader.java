package com.ssafy.yumyum.batch.nutrition;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

public class NutritionStagingItemReader implements ItemStreamReader<NutritionStagingRow>, StepExecutionListener {

    private final NutritionImportRepository repository;
    private final int pageSize;
    private long jobExecutionId;
    private long lastStagingId;
    private Iterator<NutritionStagingRow> currentPage = List.<NutritionStagingRow>of().iterator();

    public NutritionStagingItemReader(NutritionImportRepository repository, int pageSize) {
        this.repository = repository;
        this.pageSize = pageSize;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        jobExecutionId = stepExecution.getJobExecutionId();
    }

    @Override
    public NutritionStagingRow read() {
        if (!currentPage.hasNext()) {
            List<NutritionStagingRow> rows = repository.findReadyRows(jobExecutionId, lastStagingId, pageSize);
            if (rows.isEmpty()) {
                return null;
            }
            currentPage = rows.iterator();
        }
        NutritionStagingRow row = currentPage.next();
        lastStagingId = row.stagingId();
        return row;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        lastStagingId = executionContext.getLong("nutrition.staging.lastStagingId", 0L);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong("nutrition.staging.lastStagingId", lastStagingId);
    }

    @Override
    public void close() throws ItemStreamException {
    }
}
