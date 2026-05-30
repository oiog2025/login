package com.co.oscar.login.infrastructure.entrypoints.controllers;

import com.co.oscar.login.application.ports.input.ParameterInPort;
import com.co.oscar.login.infrastructure.entrypoints.dtos.ApiResponseDto;
import com.co.oscar.login.infrastructure.entrypoints.dtos.parameter.ParameterDTO;
import com.co.oscar.login.infrastructure.mapper.ParameterMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parameter")
@RequiredArgsConstructor
public class ParameterController {

  private final ParameterInPort parameterInPort;
  private final ParameterMapper parameterMapper;

  @PostMapping("/create")
  public ResponseEntity<ApiResponseDto<ParameterDTO>> createParameter(
      @RequestBody ParameterDTO parameterDTO) {
    parameterInPort.save(parameterMapper.toDomain(parameterDTO));
    return ResponseEntity.ok(
        ApiResponseDto.success(
            parameterMapper.toDTO(parameterMapper.toDomain(parameterDTO)),
            "Parameter created successfully"));
  }

  @GetMapping("/search/{name}")
  public ResponseEntity<ApiResponseDto<List<ParameterDTO>>> searchParameter(
      @PathVariable String name) {
    return ResponseEntity.ok(
        ApiResponseDto.success(
            parameterMapper.toDtoList(parameterInPort.getParametersByComponent(name)),
            "Parameters found successfully"));
  }
}
