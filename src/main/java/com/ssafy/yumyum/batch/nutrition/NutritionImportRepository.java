package com.ssafy.yumyum.batch.nutrition;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

@Repository
public class NutritionImportRepository {

    private final JdbcTemplate jdbcTemplate;

    public NutritionImportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS nutrition_import_staging (
                  staging_id BIGINT NOT NULL AUTO_INCREMENT,
                  job_execution_id BIGINT NOT NULL,
                  source_name VARCHAR(120) NOT NULL,
                  source_path VARCHAR(500) NOT NULL,
                  source_row_no INT NOT NULL,
                  raw_food_code VARCHAR(100) NULL,
                  raw_food_name VARCHAR(300) NULL,
                  raw_category VARCHAR(200) NULL,
                  raw_weight VARCHAR(100) NULL,
                  raw_energy_kcal VARCHAR(100) NULL,
                  raw_protein_g VARCHAR(100) NULL,
                  raw_fat_g VARCHAR(100) NULL,
                  raw_carbohydrate_g VARCHAR(100) NULL,
                  raw_sugar_g VARCHAR(100) NULL,
                  raw_sodium_mg VARCHAR(100) NULL,
                  raw_cholesterol_mg VARCHAR(100) NULL,
                  raw_saturated_fat_g VARCHAR(100) NULL,
                  raw_trans_fat_g VARCHAR(100) NULL,
                  raw_caffeine_mg VARCHAR(100) NULL,
                  import_status VARCHAR(20) NOT NULL DEFAULT 'READY',
                  error_message VARCHAR(500) NULL,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (staging_id),
                  UNIQUE KEY uq_nutrition_stage_job_row (job_execution_id, source_name, source_row_no),
                  INDEX idx_nutrition_stage_job_status_id (job_execution_id, import_status, staging_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS nutrition_import_report (
                  report_id BIGINT NOT NULL AUTO_INCREMENT,
                  job_execution_id BIGINT NOT NULL,
                  source_name VARCHAR(120) NOT NULL,
                  source_path VARCHAR(500) NOT NULL,
                  total_count INT NOT NULL,
                  success_count INT NOT NULL,
                  failed_count INT NOT NULL,
                  ready_count INT NOT NULL,
                  started_at DATETIME NULL,
                  finished_at DATETIME NOT NULL,
                  PRIMARY KEY (report_id),
                  UNIQUE KEY uq_nutrition_report_job (job_execution_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
    }

    public void insertStagingRows(long jobExecutionId, List<? extends RawNutritionRow> rows) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO nutrition_import_staging (
                  job_execution_id, source_name, source_path, source_row_no,
                  raw_food_code, raw_food_name, raw_category, raw_weight,
                  raw_energy_kcal, raw_protein_g, raw_fat_g, raw_carbohydrate_g,
                  raw_sugar_g, raw_sodium_mg, raw_cholesterol_mg,
                  raw_saturated_fat_g, raw_trans_fat_g, raw_caffeine_mg,
                  import_status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'READY')
                ON DUPLICATE KEY UPDATE
                  raw_food_code = VALUES(raw_food_code),
                  raw_food_name = VALUES(raw_food_name),
                  raw_category = VALUES(raw_category),
                  raw_weight = VALUES(raw_weight),
                  raw_energy_kcal = VALUES(raw_energy_kcal),
                  raw_protein_g = VALUES(raw_protein_g),
                  raw_fat_g = VALUES(raw_fat_g),
                  raw_carbohydrate_g = VALUES(raw_carbohydrate_g),
                  raw_sugar_g = VALUES(raw_sugar_g),
                  raw_sodium_mg = VALUES(raw_sodium_mg),
                  raw_cholesterol_mg = VALUES(raw_cholesterol_mg),
                  raw_saturated_fat_g = VALUES(raw_saturated_fat_g),
                  raw_trans_fat_g = VALUES(raw_trans_fat_g),
                  raw_caffeine_mg = VALUES(raw_caffeine_mg),
                  import_status = 'READY',
                  error_message = NULL
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RawNutritionRow row = rows.get(i);
                ps.setLong(1, jobExecutionId);
                ps.setString(2, row.sourceName());
                ps.setString(3, row.sourcePath());
                ps.setInt(4, row.sourceRowNo());
                ps.setString(5, row.value(TargetField.FOOD_CODE));
                ps.setString(6, row.value(TargetField.FOOD_NAME));
                ps.setString(7, row.value(TargetField.CATEGORY));
                ps.setString(8, row.value(TargetField.WEIGHT));
                ps.setString(9, row.value(TargetField.ENERGY_KCAL));
                ps.setString(10, row.value(TargetField.PROTEIN_G));
                ps.setString(11, row.value(TargetField.FAT_G));
                ps.setString(12, row.value(TargetField.CARBOHYDRATE_G));
                ps.setString(13, row.value(TargetField.SUGAR_G));
                ps.setString(14, row.value(TargetField.SODIUM_MG));
                ps.setString(15, row.value(TargetField.CHOLESTEROL_MG));
                ps.setString(16, row.value(TargetField.SATURATED_FAT_G));
                ps.setString(17, row.value(TargetField.TRANS_FAT_G));
                ps.setString(18, row.value(TargetField.CAFFEINE_MG));
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public List<NutritionStagingRow> findReadyRows(long jobExecutionId, long afterStagingId, int limit) {
        return jdbcTemplate.query("""
                SELECT *
                FROM nutrition_import_staging
                WHERE job_execution_id = ?
                  AND import_status = 'READY'
                  AND staging_id > ?
                ORDER BY staging_id
                LIMIT ?
                """, (rs, rowNum) -> new NutritionStagingRow(
                rs.getLong("staging_id"),
                rs.getLong("job_execution_id"),
                rs.getString("source_name"),
                rs.getString("source_path"),
                rs.getInt("source_row_no"),
                rs.getString("raw_food_code"),
                rs.getString("raw_food_name"),
                rs.getString("raw_category"),
                rs.getString("raw_weight"),
                rs.getString("raw_energy_kcal"),
                rs.getString("raw_protein_g"),
                rs.getString("raw_fat_g"),
                rs.getString("raw_carbohydrate_g"),
                rs.getString("raw_sugar_g"),
                rs.getString("raw_sodium_mg"),
                rs.getString("raw_cholesterol_mg"),
                rs.getString("raw_saturated_fat_g"),
                rs.getString("raw_trans_fat_g"),
                rs.getString("raw_caffeine_mg")
        ), jobExecutionId, afterStagingId, limit);
    }

    public void writeProcessResults(List<? extends NutritionProcessResult> results) {
        for (NutritionProcessResult result : results) {
            if (result.success()) {
                upsertFoodNutrition(result.food());
                jdbcTemplate.update("""
                        UPDATE nutrition_import_staging
                        SET import_status = 'DONE', error_message = NULL
                        WHERE staging_id = ?
                        """, result.stagingId());
            } else {
                jdbcTemplate.update("""
                        UPDATE nutrition_import_staging
                        SET import_status = 'FAILED', error_message = ?
                        WHERE staging_id = ?
                        """, truncate(result.errorMessage(), 500), result.stagingId());
            }
        }
    }

    public void saveReport(long jobExecutionId, String sourceName, String sourcePath,
                           LocalDateTime startedAt, LocalDateTime finishedAt) {
        NutritionImportSummary summary = summarize(jobExecutionId);
        jdbcTemplate.update("""
                INSERT INTO nutrition_import_report (
                  job_execution_id, source_name, source_path, total_count, success_count,
                  failed_count, ready_count, started_at, finished_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  total_count = VALUES(total_count),
                  success_count = VALUES(success_count),
                  failed_count = VALUES(failed_count),
                  ready_count = VALUES(ready_count),
                  finished_at = VALUES(finished_at)
                """, jobExecutionId, sourceName, sourcePath, summary.total(), summary.success(),
                summary.failed(), summary.ready(),
                timestamp(startedAt), timestamp(finishedAt));
    }

    public NutritionImportSummary summarize(long jobExecutionId) {
        return new NutritionImportSummary(
                countByStatus(jobExecutionId, null),
                countByStatus(jobExecutionId, "DONE"),
                countByStatus(jobExecutionId, "FAILED"),
                countByStatus(jobExecutionId, "READY")
        );
    }

    private void upsertFoodNutrition(FoodNutritionUpsert food) {
        jdbcTemplate.update("""
                INSERT INTO food_nutrition (
                  food_code, food_name, category, weight, energy_kcal, protein_g, fat_g,
                  carbohydrate_g, sugar_g, sodium_mg, cholesterol_mg, saturated_fat_g,
                  trans_fat_g, caffeine_mg
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  food_name = VALUES(food_name),
                  category = VALUES(category),
                  weight = VALUES(weight),
                  energy_kcal = VALUES(energy_kcal),
                  protein_g = VALUES(protein_g),
                  fat_g = VALUES(fat_g),
                  carbohydrate_g = VALUES(carbohydrate_g),
                  sugar_g = VALUES(sugar_g),
                  sodium_mg = VALUES(sodium_mg),
                  cholesterol_mg = VALUES(cholesterol_mg),
                  saturated_fat_g = VALUES(saturated_fat_g),
                  trans_fat_g = VALUES(trans_fat_g),
                  caffeine_mg = VALUES(caffeine_mg)
                """,
                food.foodCode(), food.foodName(), food.category(), food.weight(), food.energyKcal(),
                food.proteinG(), food.fatG(), food.carbohydrateG(), food.sugarG(), food.sodiumMg(),
                food.cholesterolMg(), food.saturatedFatG(), food.transFatG(), food.caffeineMg());
    }

    private int countByStatus(long jobExecutionId, String status) {
        if (status == null) {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM nutrition_import_staging
                    WHERE job_execution_id = ?
                    """, Integer.class, jobExecutionId);
            return count == null ? 0 : count;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM nutrition_import_staging
                WHERE job_execution_id = ?
                  AND import_status = ?
                """, Integer.class, jobExecutionId, status);
        return count == null ? 0 : count;
    }

    private static Timestamp timestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
