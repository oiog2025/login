package com.co.oscar.login.infrastructure.persistence.jpa.parameter;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "parameters")
@JsonPropertyOrder({
  "id",
  "channel",
  "component",
  "parameter",
  "createdAt",
  "updatedAt",
})
@Getter
@Setter
public class ParameterEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "channel", nullable = false, length = 50)
  private String channel;

  @Column(name = "component", nullable = false, length = 100)
  private String component;

  /**
   * Se usa columnDefinition = "TEXT" (o "JSON" si tu BD lo soporta de forma nativa) debido a que
   * las estructuras de parámetros de configuración suelen ser largas.
   */
  @JdbcTypeCode(SqlTypes.JSON) // ⬅️ Hibernate se encarga de la conversión a texto
  @Column(name = "parameter", nullable = false)
  private Map<String, Object> parameter;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
