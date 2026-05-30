package com.co.oscar.login.domain;

import com.co.oscar.login.domain.exceptions.ParameterException;
import java.time.LocalDateTime;
import java.util.Map;

public record Parameter(
    Long id,
    String channel,
    String component,
    Map<String, Object> parameter,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public Parameter {
    if (channel.isBlank()) {
      throw new ParameterException("channel cannot be blank");
    }
    if (component.isBlank()) {
      throw new ParameterException("component cannot be blank");
    }
    if (parameter == null) {
      throw new ParameterException("parameter cannot be null");
    }
  }
}
