package net.hytaledepot.templates.plugin.smoke;

public final class SmokeLicenseContract {
  private SmokeLicenseContract() {
  }

  public static String buildLicenseValidatePayload(String assetId, String licenseKey, String serverIp) {
    return "{"
        + "\"asset_id\":\"" + escapeJson(assetId) + "\","
        + "\"license_key\":\"" + escapeJson(licenseKey) + "\","
        + "\"server_ip\":\"" + escapeJson(serverIp) + "\","
        + "\"timestamp\":\"" + System.currentTimeMillis() + "\","
        + "\"nonce\":\"local-smoke\""
        + "}";
  }

  public static boolean isLicenseAllowed(String responseJson) {
    String body = String.valueOf(responseJson);
    return body.contains("\"allowed\":true") || body.contains("\"allowed\": true");
  }

  private static String escapeJson(String value) {
    return String.valueOf(value)
        .replace("\\", "\\\\")
        .replace("\"", "\\\"");
  }
}
