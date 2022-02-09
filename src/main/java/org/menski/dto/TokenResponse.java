package org.menski.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenResponse {
  private String accessToken;
  private String scope;
  private long expiresIn;
  private String tokenType;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (TokenResponse) obj;
    return Objects.equals(this.accessToken, that.accessToken)
        && Objects.equals(this.scope, that.scope)
        && this.expiresIn == that.expiresIn
        && Objects.equals(this.tokenType, that.tokenType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, scope, expiresIn, tokenType);
  }

  @Override
  public String toString() {
    return "TokenResponse["
        + "accessToken="
        + accessToken
        + ", "
        + "scope="
        + scope
        + ", "
        + "expiresIn="
        + expiresIn
        + ", "
        + "tokenType="
        + tokenType
        + ']';
  }
}
