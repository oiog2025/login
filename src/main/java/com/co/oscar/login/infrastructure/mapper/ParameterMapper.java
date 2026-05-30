package com.co.oscar.login.infrastructure.mapper;

import com.co.oscar.login.domain.Parameter;
import com.co.oscar.login.infrastructure.entrypoints.dtos.parameter.ParameterDTO;
import com.co.oscar.login.infrastructure.persistence.jpa.parameter.ParameterEntity;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ParameterMapper {

  Parameter toDomain(ParameterDTO parameterEntity);

  ParameterDTO toDTO(Parameter domain);

  List<Parameter> toDomainListFromDto(List<ParameterDTO> dtos);

  List<ParameterDTO> toDtoList(List<Parameter> domainParameters);

  ParameterEntity toEntity(Parameter domain);

  Parameter toDomain(ParameterEntity parameterEntity);
}
