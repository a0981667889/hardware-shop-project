package com.example.starter.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // null 欄位不序列化
@Schema(description = "統一 API 錯誤回應格式")
public class ApiErrorResponse {

    @Schema(description = "HTTP 狀態碼", example = "400")
    private int status;

    @Schema(description = "業務錯誤碼", example = "PRODUCT_NOT_FOUND")
    private String code;

    @Schema(description = "人類可讀的錯誤訊息", example = "商品不存在")
    private String message;

    @Schema(description = "發生時間")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "請求路徑", example = "/api/products/999")
    private String path;

    @Schema(description = "欄位驗證錯誤列表（驗證失敗時才有）")
    private List<FieldError> errors;

    // ─── 欄位驗證錯誤的內嵌 DTO ───
    @Getter
    @Builder
    @Schema(description = "單一欄位的驗證錯誤")
    public static class FieldError {

        @Schema(description = "欄位名稱", example = "price")
        private String field;

        @Schema(description = "被拒絕的值", example = "-100")
        private Object rejectedValue;

        @Schema(description = "錯誤原因", example = "價格不能為負數")
        private String reason;
    }
}

