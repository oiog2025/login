package com.co.oscar.login.infrastructure.persistence.jpa.parameter;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterRepository extends JpaRepository<ParameterEntity, Long> {

  List<ParameterEntity> findByComponentStartingWith(String component);
}
