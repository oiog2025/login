package com.co.oscar.login.infrastructure.entrypoints.dtos.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LoginResponseDTO {
  private String accessToken;
  private String refreshToken;
}
