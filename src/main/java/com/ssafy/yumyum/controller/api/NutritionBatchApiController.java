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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Batch API", description = "Spring Batch 수동 실행 API")
@RestController
@RequestMapping("/batch")
public class NutritionBatchApiController {

    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;

    public NutritionBatchApiController(JobOperator jobOperator, JobExplorer jobExplorer) {
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
    }

    @Operation(
            summary = "영양성분 DB 배치 적재 시작",
            description = "CSV 또는 XLSX 영양성분 파일을 staging 테이블에 적재한 뒤 정제하여 food_nutrition에 upsert합니다."
    )
    @GetMapping("/nutrition-import")
    public ResponseEntity<Map<String, Object>> startNutritionImport(
            @Parameter(description = "서버 로컬 기준 원본 파일 경로", example = "data/영양성분 DB/농림수산식품교육문화정보원_칼로리 정보_20190926.csv")
            @RequestParam String sourcePath,
            @Parameter(description = "배치 리포트에 표시할 원본 이름. 생략하면 파일명을 사용합니다.", example = "농림수산 칼로리 CSV")
            @RequestParam(required = false) String sourceName,
            @Parameter(description = "청크 크기. 1~500 사이로 보정됩니다.", example = "100")
            @RequestParam(defaultValue = "100") int chunkSize,
            @Parameter(description = "JobInstance 식별용 실행 ID. 생략하면 현재 시간을 사용합니다.", example = "manual-20260529-001")
            @RequestParam(required = false) String runId) throws Exception {

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
        parameters.setProperty("runId", runId == null || runId.isBlank()
                ? String.valueOf(System.currentTimeMillis())
                : runId);

        long executionId = jobOperator.start("nutritionImportJob", parameters);
        JobExecution execution = jobExplorer.getJobExecution(executionId);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("executionId", executionId);
        body.put("status", execution == null ? "UNKNOWN" : execution.getStatus().name());
        body.put("sourceName", parameters.getProperty("sourceName"));
        return ResponseEntity.accepted().body(body);
    }

    @Operation(
            summary = "영양성분 DB 배치 재시작",
            description = "실패 또는 중단된 nutritionImportJob 실행을 executionId 기준으로 재시작합니다."
    )
    @GetMapping("/nutrition-import/restart")
    public ResponseEntity<Map<String, Object>> restartNutritionImport(
            @Parameter(description = "재시작할 Spring Batch JobExecution ID", example = "123")
            @RequestParam long executionId) throws Exception {
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
