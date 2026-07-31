package com.example.starter.exception;

import com.example.starter.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j                    // 注入 log 物件
@RestControllerAdvice     // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    // ─── 工具方法：建立標準錯誤回應 ───────────────────────────────────
    private ApiErrorResponse buildResponse(int status, String code,
                                           String message, HttpServletRequest request) {
        return ApiErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
    }

    // ─── 1. 驗證失敗（@Valid 加在 @RequestBody 上）→ 400 ──────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // 收集所有欄位的錯誤訊息
        List<ApiErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> ApiErrorResponse.FieldError.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .reason(fe.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_FAILED")
                .message("輸入資料驗證失敗，請確認各欄位")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errors(fieldErrors)        // 附上欄位明細
                .build();

        // 驗證錯誤屬於預期的使用者錯誤，用 WARN 記錄即可
        log.warn("驗證錯誤 [{}] {}: {}",
                request.getMethod(), request.getRequestURI(), fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    // ─── 2. @Validated 方法參數驗證失敗（Query Param / Path Variable）→ 400 ───
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<ApiErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(cv -> ApiErrorResponse.FieldError.builder()
                        .field(cv.getPropertyPath().toString())
                        .rejectedValue(cv.getInvalidValue())
                        .reason(cv.getMessage())
                        .build())
                .collect(Collectors.toList());

        ApiErrorResponse body = ApiErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_FAILED")
                .message("請求參數驗證失敗")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();

        log.warn("參數驗證錯誤 [{}] {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    // ─── 3. JSON 格式錯誤（Body 解析失敗）→ 400 ──────────────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("JSON 解析失敗 [{}] {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity.badRequest().body(
                buildResponse(400, "INVALID_JSON", "請求 Body 格式錯誤，請確認 JSON 格式", request));
    }


    // ─── 5. 通用業務例外（自定義 BusinessException）→ 對應各自的 HTTP 狀態 ───
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {

        log.warn("業務錯誤 [{}] {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(ex.getHttpStatus()).body(
                buildResponse(ex.getHttpStatus().value(),
                        ex.getErrorCode().getCode(), ex.getMessage(), request));
    }

    // ─── 4.5 相容舊程式：專案原本 dto 套件的 ResourceNotFoundException 也導到 404 ───
    //     （沒有這個 handler，舊程式丟的 404 會掉進兜底 handler 變 500）
    @ExceptionHandler(com.example.starter.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleLegacyNotFound(
            com.example.starter.exception.ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildResponse(404, "RESOURCE_NOT_FOUND", ex.getMessage(), request));
    }
    // ─── 4.6 零件找不到 → 404 ─────────────────────────────────────
    @ExceptionHandler(ComponentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleComponentNotFound(
            ComponentNotFoundException ex, HttpServletRequest request) {

        log.warn("零件找不到 [{}] {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildResponse(404, "COMPONENT_NOT_FOUND", ex.getMessage(), request));
    }
    // ─── 4.7 庫存不足 → 409 ───────────────────────────────────────
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest request) {

        log.warn("庫存不足 [{}] {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                buildResponse(409, "INSUFFICIENT_STOCK", ex.getMessage(), request));
    }

    // ─── 4.8 訂單找不到 → 404 ─────────────────────────────────────
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex, HttpServletRequest request) {

        log.warn("訂單找不到 [{}] {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildResponse(404, "ORDER_NOT_FOUND", ex.getMessage(), request));
    }

    // ─── 6. 未認證（沒登入）→ 401 ────────────────────────────────────
    //     ⚠️ 注意：這個 handler 只攔得到「Controller 程式碼裡丟出」的 AuthenticationException。
    //     Spring Security 過濾器層的 401/403 到不了 @ControllerAdvice——要用坑2 的
    //     authenticationEntryPoint / accessDeniedHandler 處理（見本單元踩坑紀錄）。
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("未認證存取 [{}] {}", request.getMethod(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                buildResponse(401, "UNAUTHORIZED", "請先登入", request));
    }

    // ─── 7. 無權限 → 403 ─────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("無權限存取 [{}] {} by user", request.getMethod(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                buildResponse(403, "ACCESS_DENIED", "您沒有執行此操作的權限", request));
    }

    // ─── 8. 資料庫唯一性衝突（duplicate key）→ 409 ───────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        // 注意：不要把 ex.getMessage() 直接回傳給前端，裡面有資料表資訊！
        log.error("資料庫完整性違規 [{}] {}: {}", request.getMethod(),
                request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                buildResponse(409, "DATA_CONFLICT", "資料已存在或違反唯一性限制", request));
    }

    // ─── 9. 兜底：其他所有未預期的例外 → 500 ─────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(
            Exception ex, HttpServletRequest request) {

        // 未預期例外必須 ERROR 等級，並記錄完整 Stack Trace
        log.error("未預期例外 [{}] {}", request.getMethod(),
                request.getRequestURI(), ex);

        return ResponseEntity.internalServerError().body(
                buildResponse(500, "INTERNAL_ERROR", "伺服器發生內部錯誤，請稍後再試", request));
    }
}
