package com.ssafy.yumyum.controller.api;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health API", description = "기반 설정 점검용 API")
public class HealthApiController {

    @GetMapping
    @Operation(summary = "헬스 체크", description = "REST API 기본 구성이 정상 동작하는지 확인한다.")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(
            Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString()
            )
        );
    }
}
