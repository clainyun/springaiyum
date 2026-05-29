package com.ssafy.yumyum.batch.nutrition;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class NutritionProcessItemWriter implements ItemWriter<NutritionProcessResult> {

    private final NutritionImportRepository repository;

    public NutritionProcessItemWriter(NutritionImportRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends NutritionProcessResult> chunk) {
        repository.writeProcessResults(chunk.getItems());
    }
}
