package com.co.oscar.login.application.usescases;

import com.co.oscar.login.application.ports.input.ParameterInPort;
import com.co.oscar.login.application.ports.output.ParameterOutPort;
import com.co.oscar.login.domain.Parameter;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ParameterUseCaseImp implements ParameterInPort {
  private final ParameterOutPort parameterOutPort;

  @Override
  public Optional<Parameter> save(Parameter parameter) {
    return parameterOutPort.save(parameter);
  }

  @Override
  public List<Parameter> getParametersByComponent(String parameter) {
    return parameterOutPort.getParametersByComponent(parameter);
  }
}
