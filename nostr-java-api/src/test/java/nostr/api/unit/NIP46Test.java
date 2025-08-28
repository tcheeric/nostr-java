package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nostr.api.NIP46;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;

public class NIP46Test {

  @Test
  public void testRequestAndResponseSerialization() {
    NIP46.Request req = new NIP46.Request();
    req.setId("1");
    req.setMethod("do");
    req.addParam("a");
    String json = req.toString();
    NIP46.Request parsed = NIP46.Request.fromString(json);
    assertEquals(req.getId(), parsed.getId());
    assertEquals(req.getMethod(), parsed.getMethod());
    assertTrue(parsed.getParams().contains("a"));

    NIP46.Response resp = new NIP46.Response("1", null, "ok");
    String js = resp.toString();
    NIP46.Response parsedResp = NIP46.Response.fromString(js);
    assertEquals("ok", parsedResp.getResult());
  }

  @Test
  public void testCreateRequestEvent() {
    Identity sender = Identity.generateRandomIdentity();
    Identity signer = Identity.generateRandomIdentity();
    NIP46 nip46 = new NIP46(sender);
    NIP46.Request req = new NIP46.Request("1", "do", null);
    nip46.createRequestEvent(req, signer.getPublicKey());
    assertNotNull(nip46.getEvent());
  }
}
