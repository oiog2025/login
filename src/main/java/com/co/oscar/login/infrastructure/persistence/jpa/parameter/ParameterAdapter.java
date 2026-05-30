package com.co.oscar.login.infrastructure.persistence.jpa.parameter;

import com.co.oscar.login.application.ports.output.ParameterOutPort;
import com.co.oscar.login.domain.Parameter;
import com.co.oscar.login.infrastructure.mapper.ParameterMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParameterAdapter implements ParameterOutPort {

  private final ParameterRepository parameterJpaRepository;
  private final ParameterMapper parameterMapper;

  @Override
  public Optional<Parameter> save(Parameter parameter) {
    return Optional.ofNullable(parameter)
        .map(parameterMapper::toEntity)
        .map(parameterJpaRepository::save)
        .map(parameterMapper::toDomain);
  }

  @Override
  public List<Parameter> getParametersByComponent(String parameter) {
      if (parameter.isBlank()){
          return  Collections.emptyList();
      }
    return parameterJpaRepository.findByComponentStartingWith(parameter).stream()
        .map(parameterMapper::toDomain)
        .toList();
  }
}
