package org.menski.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.menski.Configuration;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TokenRequest(String grantType, String audience, String clientId, String clientSecret) {

  public static TokenRequest createTasklistTokenRequest() {
    return new TokenRequest("client_credentials", Configuration.getTasklistAudience(), Configuration.getZeebeClientId(), Configuration.getZeebeClientSecret());
  }
}
