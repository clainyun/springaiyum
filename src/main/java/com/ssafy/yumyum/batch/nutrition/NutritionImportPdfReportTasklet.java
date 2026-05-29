package com.ssafy.yumyum.batch.nutrition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class NutritionImportPdfReportTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(NutritionImportPdfReportTasklet.class);
    private static final Path REPORT_DIRECTORY = Path.of("reports", "batch", "nutrition");

    private final NutritionImportRepository repository;
    private final String sourceName;
    private final String sourcePath;

    public NutritionImportPdfReportTasklet(NutritionImportRepository repository, String sourceName, String sourcePath) {
        this.repository = repository;
        this.sourceName = sourceName;
        this.sourcePath = sourcePath;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws IOException {
        long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecutionId();
        LocalDateTime startedAt = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getStartTime();
        LocalDateTime finishedAt = LocalDateTime.now();

        NutritionImportSummary summary = repository.summarize(jobExecutionId);
        List<NutritionImportFailureSample> failures = repository.findFailureSamples(jobExecutionId, 20);

        Files.createDirectories(REPORT_DIRECTORY);
        Path reportPath = REPORT_DIRECTORY.resolve("nutrition-import-" + jobExecutionId + ".pdf");
        SimplePdfWriter.write(reportPath, reportLines(jobExecutionId, startedAt, finishedAt, summary, failures));

        log.info("nutritionImportJob PDF report created. jobExecutionId={}, pdfReportPath={}",
                jobExecutionId, reportPath.toAbsolutePath());
        return RepeatStatus.FINISHED;
    }

    private List<String> reportLines(long jobExecutionId, LocalDateTime startedAt, LocalDateTime finishedAt,
                                     NutritionImportSummary summary,
                                     List<NutritionImportFailureSample> failures) {
        List<String> lines = new ArrayList<>();
        lines.add("Nutrition Import Batch Report");
        lines.add("");
        lines.add("Job Execution ID: " + jobExecutionId);
        lines.add("Source Name: " + safe(sourceName));
        lines.add("Source Path: " + safe(sourcePath));
        lines.add("Started At: " + safe(startedAt));
        lines.add("Finished At: " + safe(finishedAt));
        lines.add("");
        lines.add("Total Rows: " + summary.total());
        lines.add("Success Rows: " + summary.success());
        lines.add("Failed Rows: " + summary.failed());
        lines.add("Ready Rows: " + summary.ready());
        lines.add("");
        lines.add("Failure Samples");
        if (failures.isEmpty()) {
            lines.add("- none");
        } else {
            for (NutritionImportFailureSample failure : failures) {
                lines.add("- row " + failure.sourceRowNo()
                        + " | food=" + safe(failure.rawFoodName())
                        + " | error=" + safe(failure.errorMessage()));
            }
        }
        return lines;
    }

    private static String safe(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replaceAll("[\\r\\n\\t]+", " ").trim();
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }

    private static class SimplePdfWriter {

        private SimplePdfWriter() {
        }

        static void write(Path path, List<String> lines) throws IOException {
            StringBuilder content = new StringBuilder();
            content.append("BT\n/F1 18 Tf\n50 780 Td\n");
            appendText(content, lines.isEmpty() ? "Nutrition Import Batch Report" : lines.get(0));
            content.append(" Tj\n/F1 10 Tf\n0 -28 Td\n");
            for (int i = 1; i < lines.size(); i++) {
                appendText(content, ascii(lines.get(i)));
                content.append(" Tj\n0 -14 Td\n");
            }
            content.append("ET\n");

            byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
            List<byte[]> objects = List.of(
                    "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII),
                    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.US_ASCII),
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>"
                            .getBytes(StandardCharsets.US_ASCII),
                    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII),
                    ("<< /Length " + contentBytes.length + " >>\nstream\n" + content + "endstream")
                            .getBytes(StandardCharsets.US_ASCII)
            );

            StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
            List<Integer> offsets = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                offsets.add(pdf.toString().getBytes(StandardCharsets.US_ASCII).length);
                pdf.append(i + 1).append(" 0 obj\n")
                        .append(new String(objects.get(i), StandardCharsets.US_ASCII))
                        .append("\nendobj\n");
            }

            int xrefOffset = pdf.toString().getBytes(StandardCharsets.US_ASCII).length;
            pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
            pdf.append("0000000000 65535 f \n");
            for (Integer offset : offsets) {
                pdf.append(String.format("%010d 00000 n \n", offset));
            }
            pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
            pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");

            Files.writeString(path, pdf.toString(), StandardCharsets.US_ASCII);
        }

        private static void appendText(StringBuilder builder, String text) {
            builder.append("(").append(escape(ascii(text))).append(")");
        }

        private static String escape(String value) {
            return value.replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)");
        }

        private static String ascii(String value) {
            if (value == null) {
                return "";
            }
            return value.replaceAll("[^\\x20-\\x7E]", "?");
        }
    }
}
