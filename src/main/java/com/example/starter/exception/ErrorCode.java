package com.example.starter.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 業務錯誤碼定義
 * 命名規則：[資源名稱]_[錯誤類型]
 * 例：PRODUCT_NOT_FOUND、ORDER_ALREADY_CANCELLED
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ─── 通用錯誤 ───
    VALIDATION_FAILED("VALIDATION_FAILED", "輸入資料驗證失敗"),
    INVALID_JSON("INVALID_JSON", "JSON 格式錯誤"),
    INTERNAL_ERROR("INTERNAL_ERROR", "伺服器內部錯誤"),
    ACCESS_DENIED("ACCESS_DENIED", "權限不足"),
    UNAUTHORIZED("UNAUTHORIZED", "請先登入"),

    // ─── 商品相關 ───
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "商品不存在"),
    PRODUCT_NAME_DUPLICATE("PRODUCT_NAME_DUPLICATE", "商品名稱已存在"),

    // ─── 訂單相關 ───
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "訂單不存在"),
    ORDER_ALREADY_CANCELLED("ORDER_ALREADY_CANCELLED", "訂單已取消，無法再次操作"),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "庫存不足");

    private final String code;
    private final String defaultMessage;
}

