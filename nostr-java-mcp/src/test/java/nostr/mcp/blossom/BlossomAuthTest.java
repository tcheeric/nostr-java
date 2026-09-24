package nostr.mcp.blossom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;
import nostr.mcp.tool.ToolException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the credential a Blossom server actually reads.
 *
 * <p>Everything else in this package fails loudly when it is wrong. This does not: a token with
 * the wrong encoding, a missing tag or a stale expiry produces a 401 whose body says nothing,
 * and the symptom looks identical to a misconfigured server. So the shape is asserted here,
 * against the bytes that go on the wire rather than against the object that produced them.
 */
class BlossomAuthTest {

  private static final Instant NOW = Instant.parse("2026-09-21T12:00:00Z");
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String BLOB_HASH =
      "b1674191a88ec5cdd733e4240a81803105dc412d6c6708d53ab94fc248f4f553";

  private final BlossomAuth auth = new BlossomAuth(Clock.fixed(NOW, ZoneOffset.UTC));

  // Verifies the token carries what BUD-11 requires a server to check: the reserved kind, the
  // verb matching the endpoint, an expiry in the future, and the blob the token is limited to.
  @Test
  void anUploadTokenCarriesTheVerbTheExpiryAndTheBlobHash() {
    GenericEvent token = auth.tokenFor(BlossomVerb.UPLOAD, Optional.of(BLOB_HASH));

    assertEquals(24_242, token.getKind());
    assertEquals("upload", tagValue(token, "t"));
    assertEquals(BLOB_HASH, tagValue(token, "x"));
    assertEquals(NOW.getEpochSecond(), token.getCreatedAt());
    assertTrue(
        Long.parseLong(tagValue(token, "expiration")) > NOW.getEpochSecond(),
        "the expiration must be in the future or every request is refused");
    assertFalse(token.getContent().isBlank(), "BUD-11 requires a human-readable intent");
  }

  // Verifies a list token carries no x tag. BUD-11 scopes a token to the blobs named by its x
  // tags, so an x tag on a list request would narrow the listing to one blob.
  @Test
  void aListTokenIsNotLimitedToAnyBlob() {
    GenericEvent token = auth.tokenFor(BlossomVerb.LIST, Optional.empty());

    assertEquals("list", tagValue(token, "t"));
    assertEquals("", tagValue(token, "x"));
  }

  // Verifies two tokens made at the same instant are different events. A nostr event's id is the
  // hash of its contents, so without a nonce two tokens for one verb and blob in the same second
  // share an id, and a server with a replay cache refuses the second: blossom-server answers
  // "400 Auth event already used". A list token has no x tag to vary, so two listings in one
  // second are the worst case.
  @Test
  void twoTokensMadeAtTheSameInstantAreDifferentEvents() {
    GenericEvent first = signed(auth.tokenFor(BlossomVerb.LIST, Optional.empty()));
    GenericEvent second = signed(auth.tokenFor(BlossomVerb.LIST, Optional.empty()));

    assertEquals(first.getCreatedAt(), second.getCreatedAt(), "the clock is fixed, so this holds");
    assertNotEquals(first.getId(), second.getId(), "a replayable token");
  }

  // Verifies the header is base64url without padding, as BUD-11 specifies and the reference
  // client sends. Standard base64 differs in two characters and a server rejects it with a bare
  // 401, so decoding the header back into the event is the only check that proves interop.
  @Test
  void theHeaderIsBase64UrlOfTheSignedEvent() {
    GenericEvent token = signed(auth.tokenFor(BlossomVerb.UPLOAD, Optional.of(BLOB_HASH)));

    String header = auth.headerValue(token);

    assertTrue(header.startsWith("Nostr "), header);
    String encoded = header.substring("Nostr ".length());
    assertFalse(encoded.contains("+"), "base64url uses '-', not '+'");
    assertFalse(encoded.contains("/"), "base64url uses '_', not '/'");
    assertFalse(encoded.endsWith("="), "BUD-11 specifies no padding");

    JsonNode decoded = decode(encoded);
    assertEquals(24_242, decoded.get("kind").asInt());
    assertEquals(token.getId(), decoded.get("id").asText());
    assertFalse(decoded.get("sig").asText().isBlank(), "a server verifies the signature");
  }

  // Verifies an unsigned token never reaches a server. An unsigned event is not a credential,
  // and sending one turns a programming mistake into a 401 that looks like a server problem.
  @Test
  void anUnsignedTokenIsRefusedBeforeItIsSent() {
    GenericEvent unsigned = auth.tokenFor(BlossomVerb.DELETE, Optional.of(BLOB_HASH));

    ToolException refused = assertThrows(ToolException.class, () -> auth.headerValue(unsigned));

    assertTrue(refused.getMessage().contains("unsigned"), refused.getMessage());
  }

  private GenericEvent signed(GenericEvent token) {
    Identity identity = Identity.generateRandomIdentity();
    token.setPubKey(identity.getPublicKey());
    token.update();
    identity.sign(token);
    return token;
  }

  private JsonNode decode(String encoded) {
    byte[] json = Base64.getUrlDecoder().decode(encoded);
    try {
      return MAPPER.readTree(new String(json, StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new AssertionError("the header did not decode to JSON", e);
    }
  }

  private String tagValue(GenericEvent event, String code) {
    return event.getTags().stream()
        .filter(tag -> code.equals(tag.getCode()))
        .map(tag -> ((nostr.event.tag.GenericTag) tag).getParams().get(0))
        .findFirst()
        .orElse("");
  }
}
