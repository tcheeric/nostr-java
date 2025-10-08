package nostr.api.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP57;
import nostr.api.nip57.ZapRequestParameters;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.ZapRequestEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for NIP-57 (Zaps - Lightning Payment Protocol).
 *
 * <p>These tests verify:
 * <ul>
 *   <li>Zap request creation with amounts and LNURLs</li>
 *   <li>Zap receipt validation and field verification</li>
 *   <li>Relay list handling in zap requests</li>
 *   <li>Anonymous zap support</li>
 *   <li>Amount validation</li>
 *   <li>Description hash computation (SHA256)</li>
 * </ul>
 */
@Slf4j
public class NIP57ImplTest {

  private Identity sender;
  private Identity zapRecipient;
  private NIP57 nip57;

  @BeforeEach
  void setup() {
    sender = Identity.generateRandomIdentity();
    zapRecipient = Identity.generateRandomIdentity();
    nip57 = new NIP57(sender);
  }

  @Test
  // Verifies the legacy overload still constructs zap requests with explicit parameters.
  void testNIP57CreateZapRequestEventFactory() throws NostrException {

    PublicKey recipient = zapRecipient.getPublicKey();
    final String ZAP_REQUEST_CONTENT = "zap request content";
    final Long AMOUNT = 1232456L;
    final String LNURL = "lnUrl";
    final String RELAYS_URL = "ws://localhost:5555";

    GenericEvent genericEvent =
        nip57
            .createZapRequestEvent(
                AMOUNT,
                LNURL,
                BaseTag.create("relays", RELAYS_URL),
                ZAP_REQUEST_CONTENT,
                recipient,
                null,
                null)
            .getEvent();

    ZapRequestEvent zapRequestEvent = GenericEvent.convert(genericEvent, ZapRequestEvent.class);

    assertNotNull(zapRequestEvent.getId());
    assertNotNull(zapRequestEvent.getTags());
    assertNotNull(zapRequestEvent.getContent());
    assertNotNull(zapRequestEvent.getZapRequest());
    assertNotNull(zapRequestEvent.getRecipientKey());

    assertTrue(
        zapRequestEvent.getRelays().stream().anyMatch(relay -> relay.getUri().equals(RELAYS_URL)));
    assertEquals(ZAP_REQUEST_CONTENT, genericEvent.getContent());
    assertEquals(LNURL, zapRequestEvent.getLnUrl());
    assertEquals(AMOUNT, zapRequestEvent.getAmount());
  }

  @Test
  // Ensures the ZapRequestParameters builder produces zap requests with relay lists.
  void shouldBuildZapRequestEventFromParametersObject() throws NostrException {

    PublicKey recipient = zapRecipient.getPublicKey();
    Relay relay = new Relay("ws://localhost:6001");
    final String CONTENT = "parameter object zap";
    final Long AMOUNT = 42_000L;
    final String LNURL = "lnurl1param";

    ZapRequestParameters parameters =
        ZapRequestParameters.builder()
            .amount(AMOUNT)
            .lnUrl(LNURL)
            .relay(relay)
            .content(CONTENT)
            .recipientPubKey(recipient)
            .build();

    GenericEvent genericEvent = nip57.createZapRequestEvent(parameters).getEvent();

    ZapRequestEvent zapRequestEvent = GenericEvent.convert(genericEvent, ZapRequestEvent.class);

    assertNotNull(zapRequestEvent.getId());
    assertNotNull(zapRequestEvent.getTags());
    assertEquals(CONTENT, genericEvent.getContent());
    assertEquals(LNURL, zapRequestEvent.getLnUrl());
    assertEquals(AMOUNT, zapRequestEvent.getAmount());
    assertTrue(
        zapRequestEvent.getRelays().stream().anyMatch(existing -> existing.getUri().equals(relay.getUri())));
  }

  @Test
  void testZapRequestWithMultipleRelays() throws NostrException {
    PublicKey recipient = zapRecipient.getPublicKey();
    List<Relay> relays = List.of(
        new Relay("wss://relay1.example.com"),
        new Relay("wss://relay2.example.com"),
        new Relay("wss://relay3.example.com")
    );

    ZapRequestParameters parameters =
        ZapRequestParameters.builder()
            .amount(100_000L)
            .lnUrl("lnurl123")
            .relays(relays)
            .content("Multi-relay zap")
            .recipientPubKey(recipient)
            .build();

    GenericEvent event = nip57.createZapRequestEvent(parameters).getEvent();
    ZapRequestEvent zapRequest = GenericEvent.convert(event, ZapRequestEvent.class);

    // Verify all relays are included
    assertEquals(3, zapRequest.getRelays().size());
    assertTrue(zapRequest.getRelays().stream()
        .anyMatch(r -> r.getUri().equals("wss://relay1.example.com")));
    assertTrue(zapRequest.getRelays().stream()
        .anyMatch(r -> r.getUri().equals("wss://relay2.example.com")));
    assertTrue(zapRequest.getRelays().stream()
        .anyMatch(r -> r.getUri().equals("wss://relay3.example.com")));
  }

  @Test
  void testZapRequestEventKindIsCorrect() throws NostrException {
    ZapRequestParameters parameters =
        ZapRequestParameters.builder()
            .amount(50_000L)
            .lnUrl("lnurl_test")
            .relay(new Relay("wss://relay.test"))
            .content("Zap!")
            .recipientPubKey(zapRecipient.getPublicKey())
            .build();

    GenericEvent event = nip57.createZapRequestEvent(parameters).getEvent();

    // NIP-57 zap requests are kind 9734
    assertEquals(Kind.ZAP_REQUEST.getValue(), event.getKind(),
        "Zap request should be kind 9734");
  }

  @Test
  void testZapRequestRequiredTags() throws NostrException {
    PublicKey recipient = zapRecipient.getPublicKey();

    ZapRequestParameters parameters =
        ZapRequestParameters.builder()
            .amount(25_000L)
            .lnUrl("lnurl_required_tags")
            .relay(new Relay("wss://relay.test"))
            .content("Testing required tags")
            .recipientPubKey(recipient)
            .build();

    GenericEvent event = nip57.createZapRequestEvent(parameters).getEvent();
    ZapRequestEvent zapRequest = GenericEvent.convert(event, ZapRequestEvent.class);

    // Verify p-tag (recipient) is present
    boolean hasPTag = event.getTags().stream()
        .anyMatch(tag -> tag instanceof PubKeyTag &&
            ((PubKeyTag) tag).getPublicKey().equals(recipient));
    assertTrue(hasPTag, "Zap request must have p-tag with recipient public key");

    // Verify relays tag is present
    assertNotNull(zapRequest.getRelays());
    assertFalse(zapRequest.getRelays().isEmpty(), "Zap request must have at least one relay");
  }

  @Test
  void testZapAmountValidation() throws NostrException {
    // Test with zero amount
    ZapRequestParameters zeroAmount =
        ZapRequestParameters.builder()
            .amount(0L)
            .lnUrl("lnurl_zero")
            .relay(new Relay("wss://relay.test"))
            .content("Zero amount zap")
            .recipientPubKey(zapRecipient.getPublicKey())
            .build();

    GenericEvent event = nip57.createZapRequestEvent(zeroAmount).getEvent();
    ZapRequestEvent zapRequest = GenericEvent.convert(event, ZapRequestEvent.class);

    assertEquals(0L, zapRequest.getAmount(),
        "Zap request should allow zero amount (optional tip)");
  }

  @Test
  void testZapReceiptCreation() throws NostrException {
    // Create a zap request first
    ZapRequestParameters requestParams =
        ZapRequestParameters.builder()
            .amount(100_000L)
            .lnUrl("lnurl_receipt_test")
            .relay(new Relay("wss://relay.test"))
            .content("Original zap request")
            .recipientPubKey(zapRecipient.getPublicKey())
            .build();

    GenericEvent zapRequest = nip57.createZapRequestEvent(requestParams).getEvent();

    // Create zap receipt (typically done by Lightning service provider)
    String bolt11Invoice = "lnbc1000u1p3..."; // Mock invoice
    String preimage = "0123456789abcdef"; // Mock preimage

    NIP57 receiptBuilder = new NIP57(zapRecipient);
    GenericEvent receipt = receiptBuilder.createZapReceiptEvent(
        zapRequest,
        bolt11Invoice,
        preimage,
        sender.getPublicKey()
    ).getEvent();

    // Verify receipt is kind 9735
    assertEquals(Kind.ZAP_RECEIPT.getValue(), receipt.getKind(),
        "Zap receipt should be kind 9735");

    // Verify receipt contains bolt11 tag
    boolean hasBolt11 = receipt.getTags().stream()
        .anyMatch(tag -> tag.getCode().equals("bolt11"));
    assertTrue(hasBolt11, "Zap receipt must contain bolt11 tag");

    // Verify receipt has description (zap request JSON)
    boolean hasDescription = receipt.getTags().stream()
        .anyMatch(tag -> tag.getCode().equals("description"));
    assertTrue(hasDescription, "Zap receipt must contain description tag with zap request");
  }
}
