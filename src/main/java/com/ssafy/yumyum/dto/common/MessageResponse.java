package com.ssafy.yumyum.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageResponse(
        @Schema(description = "처리 결과 메시지", example = "로그아웃되었습니다.")
        String message
) {
}
