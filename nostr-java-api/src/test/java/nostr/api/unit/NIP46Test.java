package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  // Request event should be kind NOSTR_CONNECT, include p-tag of signer, and have encrypted content.
  public void testRequestEventCompliance() {
    Identity app = Identity.generateRandomIdentity();
    Identity signer = Identity.generateRandomIdentity();
    NIP46 nip46 = new NIP46(app);
    NIP46.Request req = new NIP46.Request("42", "get_public_key", null);
    var event = nip46.createRequestEvent(req, signer.getPublicKey()).sign().getEvent();

    assertEquals(nostr.base.Kind.NOSTR_CONNECT.getValue(), event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("p")), "p-tag must be present");
    assertNotNull(event.getContent());
    assertFalse(event.getContent().isEmpty());
  }

  @Test
  // Response event should also be kind NOSTR_CONNECT and include app p-tag.
  public void testResponseEventCompliance() {
    Identity signer = Identity.generateRandomIdentity();
    Identity app = Identity.generateRandomIdentity();
    NIP46 nip46 = new NIP46(signer);
    NIP46.Response resp = new NIP46.Response("42", null, "ok");
    var event = nip46.createResponseEvent(resp, app.getPublicKey()).sign().getEvent();
    assertEquals(nostr.base.Kind.NOSTR_CONNECT.getValue(), event.getKind());
    assertTrue(event.getTags().stream().anyMatch(t -> t.getCode().equals("p")));
  }

  @Test
  // Multi-parameter request should serialize deterministically and decrypt to original payload.
  public void testMultiParamRequestRoundTrip() {
    Identity app = Identity.generateRandomIdentity();
    Identity signer = Identity.generateRandomIdentity();
    NIP46 nip46 = new NIP46(app);

    NIP46.Request req = new NIP46.Request("7", "sign_event", null);
    req.addParam("kind=1");
    req.addParam("tag=p:abcd");

    var ev = nip46.createRequestEvent(req, signer.getPublicKey()).sign().getEvent();
    assertEquals(nostr.base.Kind.NOSTR_CONNECT.getValue(), ev.getKind());

    String decrypted = nostr.api.NIP44.decrypt(signer, ev);
    NIP46.Request parsed = NIP46.Request.fromString(decrypted);
    assertEquals("7", parsed.getId());
    assertEquals("sign_event", parsed.getMethod());
    assertTrue(parsed.getParams().contains("kind=1"));
    assertTrue(parsed.getParams().contains("tag=p:abcd"));
  }
}
