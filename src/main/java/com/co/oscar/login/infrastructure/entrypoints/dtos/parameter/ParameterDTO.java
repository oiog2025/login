package com.co.oscar.login.infrastructure.entrypoints.dtos.parameter;

import java.time.LocalDateTime;
import java.util.Map;

public record ParameterDTO(
    Long id,
    String channel,
    String component,
    Map<String, Object> parameter,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
