package com.ssafy.yumyum.controller.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch")
public class NutritionBatchApiController {

    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    public NutritionBatchApiController(JobOperator jobOperator, JobExplorer jobExplorer) {
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
    }

    @GetMapping("/nutrition-import")
    public ResponseEntity<Map<String, Object>> startNutritionImport(
            @RequestParam String sourcePath,
            @RequestParam(required = false) String sourceName,
            @RequestParam(defaultValue = "100") int chunkSize,
            @RequestParam(required = false) String runTime) throws Exception {

        Path path = Path.of(sourcePath);
        if (!Files.exists(path)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "sourcePath file was not found.",
                    "sourcePath", sourcePath
            ));
        }

        Properties parameters = new Properties();
        parameters.setProperty("sourcePath", path.toString());
        parameters.setProperty("sourceName", sourceName(path, sourceName));
        parameters.setProperty("chunkSize", String.valueOf(Math.max(1, Math.min(chunkSize, 500))));
        parameters.setProperty("runTime", runTime == null || runTime.isBlank()
                ? String.valueOf(System.currentTimeMillis())
                : runTime);

        long executionId = jobOperator.start("nutritionImportJob", parameters);
        JobExecution execution = jobExplorer.getJobExecution(executionId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionId", executionId);
        body.put("status", execution == null ? "UNKNOWN" : execution.getStatus().name());
        body.put("sourceName", parameters.getProperty("sourceName"));
        return ResponseEntity.accepted().body(body);
    }

    @GetMapping("/nutrition-import/restart")
    public ResponseEntity<Map<String, Object>> restartNutritionImport(@RequestParam long executionId) throws Exception {
        long restartedExecutionId = jobOperator.restart(executionId);
        JobExecution execution = jobExplorer.getJobExecution(restartedExecutionId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionId", restartedExecutionId);
        body.put("status", execution == null ? "UNKNOWN" : execution.getStatus().name());
        body.put("restartedFrom", executionId);
        return ResponseEntity.accepted().body(body);
    }

    private static String sourceName(Path sourcePath, String sourceName) {
        if (sourceName != null && !sourceName.isBlank()) {
            return sourceName;
        }
        return sourcePath.getFileName().toString();
    }
}
