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
 * NIP-57 helpers (Zaps). Build zap request/receipt events and related tags.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/57.md">NIP-57</a>
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
