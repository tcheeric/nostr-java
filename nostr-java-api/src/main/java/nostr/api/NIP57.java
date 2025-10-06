package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.nip01.NIP01TagFactory;
import nostr.api.nip57.NIP57TagFactory;
import nostr.api.nip57.NIP57ZapReceiptBuilder;
import nostr.api.nip57.NIP57ZapRequestBuilder;
import nostr.api.nip57.ZapRequestParameters;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.entities.ZapRequest;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;

/**
 * NIP-57: Lightning Zaps.
 *
 * <p>This class provides utilities for creating and managing Lightning Network zaps on Nostr. Zaps
 * are a standardized way to send Bitcoin payments (via Lightning Network) to Nostr users, content,
 * or events, with the payment being publicly recorded on Nostr relays.
 *
 * <h2>What are Zaps?</h2>
 *
 * <p>Zaps enable Bitcoin micropayments on Nostr:
 * <ul>
 *   <li><strong>Zap Request (kind 9734):</strong> A request to send sats to a user or event</li>
 *   <li><strong>Zap Receipt (kind 9735):</strong> Public proof that a payment was completed</li>
 *   <li><strong>Lightning Integration:</strong> Uses LNURL and Lightning invoices (bolt11)</li>
 *   <li><strong>Public Attribution:</strong> Zaps are publicly visible on relays (unlike tips)</li>
 * </ul>
 *
 * <h2>How Zaps Work</h2>
 *
 * <ol>
 *   <li><strong>User creates a zap request</strong> (kind 9734) specifying amount and recipient</li>
 *   <li><strong>Request is sent to an LNURL server</strong> (specified in recipient's NIP-05 profile)</li>
 *   <li><strong>LNURL server returns a Lightning invoice</strong> (bolt11)</li>
 *   <li><strong>User pays the invoice</strong> via their Lightning wallet</li>
 *   <li><strong>LNURL server publishes a zap receipt</strong> (kind 9735) to Nostr relays</li>
 *   <li><strong>Receipt is visible to everyone</strong> as proof of payment</li>
 * </ol>
 *
 * <h2>Zap Types</h2>
 *
 * <ul>
 *   <li><strong>Public Zaps:</strong> Sender is visible (default)</li>
 *   <li><strong>Private Zaps:</strong> Sender is anonymous (requires NIP-04 encryption)</li>
 *   <li><strong>Profile Zaps:</strong> Zap a user's profile</li>
 *   <li><strong>Event Zaps:</strong> Zap a specific note or event</li>
 *   <li><strong>Anonymous Zaps:</strong> No sender attribution</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Example 1: Create a Zap Request (Profile Zap)</h3>
 * <pre>{@code
 * Identity sender = new Identity("nsec1...");
 * PublicKey recipient = new PublicKey("npub1...");
 *
 * NIP57 nip57 = new NIP57(sender);
 * nip57.createZapRequestEvent(
 *         1000L,                              // amount in millisatoshis
 *         "lnurl1...",                        // LNURL from recipient's profile
 *         List.of("wss://relay.damus.io"),   // relays to publish receipt
 *         "Great content! ⚡",                // optional comment
 *         recipient                           // recipient public key
 *     )
 *     .sign()
 *     .send(relays); // sends to LNURL server (not relays)
 * }</pre>
 *
 * <h3>Example 2: Create a Zap Request (Event Zap)</h3>
 * <pre>{@code
 * Identity sender = new Identity("nsec1...");
 * GenericEvent noteToZap = ... // the note you want to zap
 * PublicKey author = noteToZap.getPubKey();
 *
 * NIP57 nip57 = new NIP57(sender);
 * nip57.createZapRequestEvent(
 *         5000L,                              // 5000 millisats
 *         "lnurl1...",                        // author's LNURL
 *         List.of("wss://relay.damus.io"),
 *         "Amazing post! 🔥",
 *         author,
 *         noteToZap,                          // the event being zapped
 *         null                                // no address tag (for kind 1 events)
 *     )
 *     .sign()
 *     .send(relays);
 * }</pre>
 *
 * <h3>Example 3: Create a Zap Request with Parameter Object</h3>
 * <pre>{@code
 * Identity sender = new Identity("nsec1...");
 * PublicKey recipient = new PublicKey("npub1...");
 *
 * ZapRequestParameters params = ZapRequestParameters.builder()
 *     .amount(1000L)
 *     .lnUrl("lnurl1...")
 *     .relays(List.of(new Relay("wss://relay.damus.io")))
 *     .content("Thanks for the content!")
 *     .recipientPubKey(recipient)
 *     .build();
 *
 * NIP57 nip57 = new NIP57(sender);
 * nip57.createZapRequestEvent(params)
 *     .sign()
 *     .send(relays);
 * }</pre>
 *
 * <h3>Example 4: Create a Zap Receipt (LNURL server use case)</h3>
 * <pre>{@code
 * // This is typically done by the LNURL server after payment is confirmed
 * Identity lnurlServer = new Identity("nsec_of_lnurl_server...");
 * GenericEvent zapRequest = ... // the original zap request
 * String bolt11 = "lnbc..."; // the paid Lightning invoice
 * String preimage = "..."; // payment preimage (proof of payment)
 * PublicKey recipient = zapRequest.getPubKey();
 *
 * NIP57 nip57 = new NIP57(lnurlServer);
 * nip57.createZapReceiptEvent(zapRequest, bolt11, preimage, recipient)
 *     .sign()
 *     .send(relays); // publishes receipt to Nostr
 * }</pre>
 *
 * <h2>Design Pattern</h2>
 *
 * <p>This class follows the <strong>Facade Pattern</strong> combined with <strong>Builder Pattern</strong>:
 * <ul>
 *   <li><strong>Facade:</strong> Simplifies zap request/receipt creation</li>
 *   <li><strong>Builder:</strong> {@link NIP57ZapRequestBuilder} and {@link NIP57ZapReceiptBuilder} handle construction</li>
 *   <li><strong>Parameter Object:</strong> {@link ZapRequestParameters} groups related parameters</li>
 *   <li><strong>Method Chaining:</strong> Fluent API for sign() and send()</li>
 * </ul>
 *
 * <h2>Key Concepts</h2>
 *
 * <h3>Amount (millisatoshis)</h3>
 * <p>Amounts are specified in millisatoshis (msat = 1/1000 of a satoshi = 1/100,000,000,000 BTC).
 * <ul>
 *   <li>1 satoshi = 1,000 millisatoshis</li>
 *   <li>Example: 1000 msat = 1 sat ≈ $0.0006 USD (at $60k BTC)</li>
 * </ul>
 *
 * <h3>LNURL</h3>
 * <p>Lightning URL (LNURL) is a protocol for Lightning payments. The recipient's LNURL is typically
 * found in their NIP-05 profile metadata. The LNURL server generates invoices and publishes receipts.
 *
 * <h3>Bolt11</h3>
 * <p>Bolt11 is the Lightning invoice format. It's a bech32-encoded payment request that includes:
 * <ul>
 *   <li>Payment amount</li>
 *   <li>Payment hash</li>
 *   <li>Expiration time</li>
 *   <li>Routing hints</li>
 * </ul>
 *
 * <h2>Event Tags</h2>
 *
 * <p>Zap requests (kind 9734) include:
 * <ul>
 *   <li><strong>relays tag:</strong> Where the zap receipt should be published</li>
 *   <li><strong>amount tag:</strong> Payment amount in millisatoshis</li>
 *   <li><strong>lnurl tag:</strong> LNURL of the recipient</li>
 *   <li><strong>p tag:</strong> Recipient's public key (optional for event zaps)</li>
 *   <li><strong>e tag:</strong> Event ID being zapped (optional)</li>
 *   <li><strong>a tag:</strong> Address tag for replaceable/parameterized events (optional)</li>
 * </ul>
 *
 * <p>Zap receipts (kind 9735) include:
 * <ul>
 *   <li><strong>bolt11 tag:</strong> The Lightning invoice that was paid</li>
 *   <li><strong>preimage tag:</strong> Payment preimage (proof of payment)</li>
 *   <li><strong>description tag:</strong> JSON-encoded zap request event</li>
 *   <li><strong>p tag:</strong> Recipient's public key</li>
 *   <li><strong>e tag:</strong> Original event ID (if event zap)</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This class is <strong>not thread-safe</strong> for instance methods. Each thread should create
 * its own {@code NIP57} instance.
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/57.md">NIP-57 Specification</a>
 * @see NIP57ZapRequestBuilder
 * @see NIP57ZapReceiptBuilder
 * @see ZapRequestParameters
 * @since 0.3.0
 */
public class NIP57 extends EventNostr {

  private final NIP57ZapRequestBuilder zapRequestBuilder;
  private final NIP57ZapReceiptBuilder zapReceiptBuilder;

  public NIP57(@NonNull Identity sender) {
    super(sender);
    this.zapRequestBuilder = new NIP57ZapRequestBuilder(sender);
    this.zapReceiptBuilder = new NIP57ZapReceiptBuilder(sender);
  }

  @Override
  public NIP57 setSender(@NonNull Identity sender) {
    super.setSender(sender);
    this.zapRequestBuilder.updateDefaultSender(sender);
    this.zapReceiptBuilder.updateDefaultSender(sender);
    return this;
  }

  /**
   * Create a zap request event (kind 9734) using a structured request.
   */
  public NIP57 createZapRequestEvent(
      @NonNull ZapRequest zapRequest,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    this.updateEvent(
        zapRequestBuilder.buildFromZapRequest(
            resolveSender(), zapRequest, content, recipientPubKey, zappedEvent, addressTag));
    return this;
  }

  /**
   * Create a zap request event (kind 9734) using a parameter object.
   */
  public NIP57 createZapRequestEvent(@NonNull ZapRequestParameters parameters) {
    this.updateEvent(zapRequestBuilder.build(parameters));
    return this;
  }

  /**
   * Create a zap request event (kind 9734) using explicit parameters and a relays tag.
   */
  public NIP57 createZapRequestEvent(
      @NonNull Long amount,
      @NonNull String lnUrl,
      @NonNull BaseTag relaysTags,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    return createZapRequestEvent(
        ZapRequestParameters.builder()
            .amount(amount)
            .lnUrl(lnUrl)
            .relaysTag(requireRelaysTag(relaysTags))
            .content(content)
            .recipientPubKey(recipientPubKey)
            .zappedEvent(zappedEvent)
            .addressTag(addressTag)
            .build());
  }

  /**
   * Create a zap request event (kind 9734) using explicit parameters and a list of relays.
   */
  public NIP57 createZapRequestEvent(
      @NonNull Long amount,
      @NonNull String lnUrl,
      @NonNull List<Relay> relays,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    return createZapRequestEvent(
        ZapRequestParameters.builder()
            .amount(amount)
            .lnUrl(lnUrl)
            .relays(relays)
            .content(content)
            .recipientPubKey(recipientPubKey)
            .zappedEvent(zappedEvent)
            .addressTag(addressTag)
            .build());
  }

  /**
   * Create a zap request event (kind 9734) using explicit parameters and a list of relay URLs.
   */
  public NIP57 createZapRequestEvent(
      @NonNull Long amount,
      @NonNull String lnUrl,
      @NonNull List<String> relays,
      @NonNull String content,
      PublicKey recipientPubKey) {
    return createZapRequestEvent(
        ZapRequestParameters.builder()
            .amount(amount)
            .lnUrl(lnUrl)
            .relays(relays.stream().map(Relay::new).toList())
            .content(content)
            .recipientPubKey(recipientPubKey)
            .build());
  }

  /**
   * Create a zap receipt event (kind 9735) acknowledging a zap payment.
   */
  public NIP57 createZapReceiptEvent(
      @NonNull GenericEvent zapRequestEvent,
      @NonNull String bolt11,
      @NonNull String preimage,
      @NonNull PublicKey zapRecipient) {
    this.updateEvent(zapReceiptBuilder.build(zapRequestEvent, bolt11, preimage, zapRecipient));
    return this;
  }

  public NIP57 addLnurlTag(@NonNull String lnurl) {
    getEvent().addTag(NIP57TagFactory.lnurl(lnurl));
    return this;
  }

  public NIP57 addEventTag(@NonNull EventTag tag) {
    getEvent().addTag(tag);
    return this;
  }

  public NIP57 addBolt11Tag(@NonNull String bolt11) {
    getEvent().addTag(NIP57TagFactory.bolt11(bolt11));
    return this;
  }

  public NIP57 addPreImageTag(@NonNull String preimage) {
    getEvent().addTag(NIP57TagFactory.preimage(preimage));
    return this;
  }

  public NIP57 addDescriptionTag(@NonNull String description) {
    getEvent().addTag(NIP57TagFactory.description(description));
    return this;
  }

  public NIP57 addAmountTag(@NonNull Integer amount) {
    getEvent().addTag(NIP57TagFactory.amount(amount));
    return this;
  }

  public NIP57 addRecipientTag(@NonNull PublicKey recipient) {
    getEvent().addTag(NIP01TagFactory.pubKeyTag(recipient));
    return this;
  }

  public NIP57 addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    getEvent().addTag(NIP57TagFactory.zap(receiver, relays, weight));
    return this;
  }

  public NIP57 addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    getEvent().addTag(NIP57TagFactory.zap(receiver, relays));
    return this;
  }

  public NIP57 addRelaysTag(@NonNull RelaysTag relaysTag) {
    getEvent().addTag(relaysTag);
    return this;
  }

  public NIP57 addRelaysList(@NonNull List<Relay> relays) {
    return addRelaysTag(new RelaysTag(relays));
  }

  public NIP57 addRelays(@NonNull List<String> relays) {
    return addRelaysList(relays.stream().map(Relay::new).toList());
  }

  public NIP57 addRelays(@NonNull String... relays) {
    return addRelays(List.of(relays));
  }

  public static BaseTag createLnurlTag(@NonNull String lnurl) {
    return NIP57TagFactory.lnurl(lnurl);
  }

  public static BaseTag createBolt11Tag(@NonNull String bolt11) {
    return NIP57TagFactory.bolt11(bolt11);
  }

  public static BaseTag createPreImageTag(@NonNull String preimage) {
    return NIP57TagFactory.preimage(preimage);
  }

  public static BaseTag createDescriptionTag(@NonNull String description) {
    return NIP57TagFactory.description(description);
  }

  public static BaseTag createAmountTag(@NonNull Number amount) {
    return NIP57TagFactory.amount(amount);
  }

  public static BaseTag createZapSenderPubKeyTag(@NonNull PublicKey publicKey) {
    return NIP57TagFactory.zapSender(publicKey);
  }

  public static BaseTag createZapTag(
      @NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    return NIP57TagFactory.zap(receiver, relays, weight);
  }

  public static BaseTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    return NIP57TagFactory.zap(receiver, relays);
  }

  private RelaysTag requireRelaysTag(BaseTag tag) {
    if (tag instanceof RelaysTag relaysTag) {
      return relaysTag;
    }
    throw new IllegalArgumentException("tag must be of type RelaysTag");
  }

  private Identity resolveSender() {
    Identity sender = getSender();
    if (sender == null) {
      throw new IllegalStateException("Sender identity is required for zap operations");
    }
    return sender;
  }
}
