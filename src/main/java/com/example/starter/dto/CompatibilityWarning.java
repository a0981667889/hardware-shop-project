package com.example.starter.dto;

import java.util.List;

public record CompatibilityWarning(String message, List<Long> componentIds) {
}
