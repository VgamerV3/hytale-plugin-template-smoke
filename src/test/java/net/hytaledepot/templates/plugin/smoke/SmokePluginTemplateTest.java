package net.hytaledepot.templates.plugin.smoke;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class SmokePluginTemplateTest {
  @Test
  void buildsLicensePayloadWithExpectedContractFields() {
    String payload = SmokeLicenseContract.buildLicenseValidatePayload("asset-42", "HD-KEY-0001", "203.0.113.10");

    // Contract checks keep the payload shape aligned with the Dev Docs examples.
    assertTrue(payload.contains("\"asset_id\":\"asset-42\""));
    assertTrue(payload.contains("\"license_key\":\"HD-KEY-0001\""));
    assertTrue(payload.contains("\"server_ip\":\"203.0.113.10\""));
    assertTrue(payload.contains("\"nonce\":\"local-smoke\""));
  }

  @Test
  void parsesAllowedFieldDeterministically() {
    // Parser must accept both compact and spaced boolean forms.
    assertTrue(SmokeLicenseContract.isLicenseAllowed("{\"allowed\":true}"));
    assertTrue(SmokeLicenseContract.isLicenseAllowed("{\"allowed\": true}"));
    assertFalse(SmokeLicenseContract.isLicenseAllowed("{\"allowed\":false}"));
  }
}
