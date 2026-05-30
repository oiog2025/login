package com.co.oscar.login.application.ports.input;

import com.co.oscar.login.domain.Parameter;
import java.util.List;
import java.util.Optional;

public interface ParameterInPort {

  Optional<Parameter> save(Parameter parameter);

  List<Parameter> getParametersByComponent(String parameter);
}
