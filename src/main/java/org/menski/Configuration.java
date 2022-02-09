package org.menski;

public class Configuration {

  public static String getZeebeAddress() {
    return System.getenv("ZEEBE_ADDRESS");
  }

  public static String getBaseDomain() {
    return getZeebeAddress().split(":")[0].split("\\.", 4)[3];
  }

  public static String getZeebeAudience() {
    return String.format("zeebe.%s", getBaseDomain());
  }

  public static String getTasklistAudience() {
    return String.format("tasklist.%s", getBaseDomain());
  }

  public static String getTasklistAddress() {
    String[] parts = getZeebeAddress().split(":")[0].split("\\.", 4);
    return String.format("https://%s.tasklist.%s/%s", parts[1], parts[3], parts[0]);
  }

  public static String getZeebeClientId() {
    return System.getenv("ZEEBE_CLIENT_ID");
  }

  public static String getZeebeClientSecret() {
    return System.getenv("ZEEBE_CLIENT_SECRET");
  }

  public static String getZeebeAuthorizationServerUrl() {
    return System.getenv("ZEEBE_AUTHORIZATION_SERVER_URL");
  }

  public static int getZeebeInstances() {
    String instances = System.getenv("ZEEBE_INSTANCES");
    if (instances != null && !instances.isBlank()) {
      try {
        return Integer.parseInt(instances);
      } catch (Exception e) {
        // ignore
      }
    }

    // default value
    return 100;
  }
}
