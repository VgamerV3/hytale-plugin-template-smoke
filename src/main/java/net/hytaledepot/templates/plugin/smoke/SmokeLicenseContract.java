package net.hytaledepot.templates.plugin.smoke;

public final class SmokeLicenseContract {
  private SmokeLicenseContract() {
  }

  // Build the same payload shape shown in the Dev Docs licensing section.
  public static String buildLicenseValidatePayload(String assetId, String licenseKey, String serverIp) {
    return "{"
        + "\"asset_id\":\"" + assetId + "\","
        + "\"license_key\":\"" + licenseKey + "\","
        + "\"server_ip\":\"" + serverIp + "\","
        + "\"timestamp\":\"" + System.currentTimeMillis() + "\","
        + "\"nonce\":\"local-smoke\""
        + "}";
  }

  // Lightweight parser used by tests to validate the response contract shape.
  public static boolean isLicenseAllowed(String responseJson) {
    String body = String.valueOf(responseJson);
    return body.contains("\"allowed\":true") || body.contains("\"allowed\": true");
  }
}
