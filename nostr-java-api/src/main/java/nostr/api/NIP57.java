package nostr.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.ZapRequest;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;
import org.apache.commons.text.StringEscapeUtils;
import nostr.event.json.codec.EventEncodingException;

/**
 * NIP-57 helpers (Zaps). Build zap request/receipt events and related tags.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/57.md">NIP-57</a>
 */
public class NIP57 extends EventNostr {

  public NIP57(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a zap request event (kind 9734) using a structured request.
   *
   * @param zapRequest the zap request details (amount, lnurl, relays)
   * @param content optional human-readable note/comment
   * @param recipientPubKey optional pubkey of the zap recipient (p-tag)
   * @param zappedEvent optional event being zapped (e-tag)
   * @param addressTag optional address tag (a-tag) for addressable events
   * @return this instance for chaining
   */
  public NIP57 createZapRequestEvent(
      @NonNull ZapRequest zapRequest,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.ZAP_REQUEST, content).create();

    genericEvent.addTag(zapRequest.getRelaysTag());
    genericEvent.addTag(createAmountTag(zapRequest.getAmount()));
    genericEvent.addTag(createLnurlTag(zapRequest.getLnUrl()));

    if (recipientPubKey != null) {
      genericEvent.addTag(NIP01.createPubKeyTag(recipientPubKey));
    }

    if (zappedEvent != null) {
      genericEvent.addTag(NIP01.createEventTag(zappedEvent.getId()));
    }

    if (addressTag != null) {
      if (!Constants.Tag.ADDRESS_CODE.equals(addressTag.getCode())) { // Sanity check
        throw new IllegalArgumentException("tag must be of type AddressTag");
      }
      genericEvent.addTag(addressTag);
    }

    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a zap request event (kind 9734) using explicit parameters and a relays tag.
   *
   * @param amount the zap amount in millisats
   * @param lnUrl the LNURL pay endpoint
   * @param relaysTags relays tag listing recommended relays (relays tag)
   * @param content optional human-readable note/comment
   * @param recipientPubKey optional pubkey of the zap recipient (p-tag)
   * @param zappedEvent optional event being zapped (e-tag)
   * @param addressTag optional address tag (a-tag) for addressable events
   * @return this instance for chaining
   */
  public NIP57 createZapRequestEvent(
      @NonNull Long amount,
      @NonNull String lnUrl,
      @NonNull BaseTag relaysTags,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {

    if (!(relaysTags instanceof RelaysTag)) {
      throw new IllegalArgumentException("tag must be of type RelaysTag");
    }

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.ZAP_REQUEST, content).create();

    genericEvent.addTag(relaysTags);
    genericEvent.addTag(createAmountTag(amount));
    genericEvent.addTag(createLnurlTag(lnUrl));

    if (recipientPubKey != null) {
      genericEvent.addTag(NIP01.createPubKeyTag(recipientPubKey));
    }

    if (zappedEvent != null) {
      genericEvent.addTag(NIP01.createEventTag(zappedEvent.getId()));
    }

    if (addressTag != null) {
      if (!(addressTag instanceof nostr.event.tag.AddressTag)) { // Sanity check
        throw new IllegalArgumentException("Address tag must be of type AddressTag");
      }
      genericEvent.addTag(addressTag);
    }

    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a zap request event (kind 9734) using explicit parameters and a list of relays.
   *
   * @param amount the zap amount in millisats
   * @param lnUrl the LNURL pay endpoint
   * @param relays the list of recommended relays
   * @param content optional human-readable note/comment
   * @param recipientPubKey optional pubkey of the zap recipient (p-tag)
   * @param zappedEvent optional event being zapped (e-tag)
   * @param addressTag optional address tag (a-tag) for addressable events
   * @return this instance for chaining
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
        amount, lnUrl, new RelaysTag(relays), content, recipientPubKey, zappedEvent, addressTag);
  }

  /**
   * Create a zap request event (kind 9734) using explicit parameters and a list of relay URLs.
   *
   * @param amount the zap amount in millisats
   * @param lnUrl the LNURL pay endpoint
   * @param relays list of relay URLs
   * @param content optional human-readable note/comment
   * @param recipientPubKey optional pubkey of the zap recipient (p-tag)
   * @return this instance for chaining
   */
  public NIP57 createZapRequestEvent(
      @NonNull Long amount,
      @NonNull String lnUrl,
      @NonNull List<String> relays,
      @NonNull String content,
      PublicKey recipientPubKey) {

    return createZapRequestEvent(
        amount,
        lnUrl,
        relays.stream().map(Relay::new).toList(),
        content,
        recipientPubKey,
        null,
        null);
  }

  /**
   * Create a zap receipt event (kind 9735) acknowledging a zap payment.
   *
   * @param zapRequestEvent the original zap request event
   * @param bolt11 the BOLT11 invoice
   * @param preimage the preimage for the invoice
   * @param zapRecipient the zap recipient pubkey (p-tag)
   * @return this instance for chaining
   */
  public NIP57 createZapReceiptEvent(
      @NonNull GenericEvent zapRequestEvent,
      @NonNull String bolt11,
      @NonNull String preimage,
      @NonNull PublicKey zapRecipient) {

    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.ZAP_RECEIPT, "").create();

    // Add the tags
    genericEvent.addTag(NIP01.createPubKeyTag(zapRecipient));

    // Zap receipt tags
    try {
      String descriptionSha256 = IEvent.MAPPER_BLACKBIRD.writeValueAsString(zapRequestEvent);
      genericEvent.addTag(createDescriptionTag(StringEscapeUtils.escapeJson(descriptionSha256)));
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to encode zap receipt description", ex);
    }
    genericEvent.addTag(createBolt11Tag(bolt11));
    genericEvent.addTag(createPreImageTag(preimage));
    genericEvent.addTag(createZapSenderPubKeyTag(zapRequestEvent.getPubKey()));
    genericEvent.addTag(NIP01.createEventTag(zapRequestEvent.getId()));

    nostr.event.filter.Filterable
        .getTypeSpecificTags(nostr.event.tag.AddressTag.class, zapRequestEvent)
        .stream()
        .findFirst()
        .ifPresent(genericEvent::addTag);

    genericEvent.setCreatedAt(zapRequestEvent.getCreatedAt());

    // Set the event
    this.updateEvent(genericEvent);

    // Return this
    return this;
  }

  public NIP57 addLnurlTag(@NonNull String lnurl) {
    getEvent().addTag(createLnurlTag(lnurl));
    return this;
  }

  /**
   * Add an event tag (e-tag) to the current zap-related event.
   *
   * @param tag the event tag
   * @return this instance for chaining
   */
  public NIP57 addEventTag(@NonNull EventTag tag) {
    getEvent().addTag(tag);
    return this;
  }

  /**
   * Add a bolt11 tag to the current event.
   *
   * @param bolt11 the BOLT11 invoice
   * @return this instance for chaining
   */
  public NIP57 addBolt11Tag(@NonNull String bolt11) {
    getEvent().addTag(createBolt11Tag(bolt11));
    return this;
  }

  /**
   * Add a preimage tag to the current event.
   *
   * @param preimage the payment preimage
   * @return this instance for chaining
   */
  public NIP57 addPreImageTag(@NonNull String preimage) {
    getEvent().addTag(createPreImageTag(preimage));
    return this;
  }

  /**
   * Add a description tag to the current event.
   *
   * @param description a human-readable description or escaped JSON
   * @return this instance for chaining
   */
  public NIP57 addDescriptionTag(@NonNull String description) {
    getEvent().addTag(createDescriptionTag(description));
    return this;
  }

  /**
   * Add an amount tag to the current event.
   *
   * @param amount the amount (typically millisats)
   * @return this instance for chaining
   */
  public NIP57 addAmountTag(@NonNull Integer amount) {
    getEvent().addTag(createAmountTag(amount));
    return this;
  }

  /**
   * Add a p-tag recipient to the current event.
   *
   * @param recipient the recipient public key
   * @return this instance for chaining
   */
  public NIP57 addRecipientTag(@NonNull PublicKey recipient) {
    getEvent().addTag(NIP01.createPubKeyTag(recipient));
    return this;
  }

  /**
   * Add a zap tag listing receiver and relays, with an optional weight.
   *
   * @param receiver the zap receiver public key
   * @param relays list of recommended relays
   * @param weight optional splitting weight
   * @return this instance for chaining
   */
  public NIP57 addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    getEvent().addTag(createZapTag(receiver, relays, weight));
    return this;
  }

  /**
   * Add a zap tag listing receiver and relays.
   *
   * @param receiver the zap receiver public key
   * @param relays list of recommended relays
   * @return this instance for chaining
   */
  public NIP57 addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    getEvent().addTag(createZapTag(receiver, relays));
    return this;
  }

  /**
   * Add a relays tag to the current event.
   *
   * @param relaysTag the relays tag
   * @return this instance for chaining
   */
  public NIP57 addRelaysTag(@NonNull RelaysTag relaysTag) {
    getEvent().addTag(relaysTag);
    return this;
  }

  /**
   * Add a relays tag built from a list of relay objects.
   *
   * @param relays list of relay objects
   * @return this instance for chaining
   */
  public NIP57 addRelaysList(@NonNull List<Relay> relays) {
    return addRelaysTag(new RelaysTag(relays));
  }

  /**
   * Add a relays tag built from a list of relay URLs.
   *
   * @param relays list of relay URLs
   * @return this instance for chaining
   */
  public NIP57 addRelays(@NonNull List<String> relays) {
    return addRelaysList(relays.stream().map(Relay::new).toList());
  }

  /**
   * Add a relays tag built from relay URL varargs.
   *
   * @param relays relay URL strings
   * @return this instance for chaining
   */
  public NIP57 addRelays(@NonNull String... relays) {
    return addRelays(List.of(relays));
  }

  /**
   * Create a lnurl tag.
   *
   * @param lnurl the LNURL pay endpoint
   * @return the created tag
   */
  public static BaseTag createLnurlTag(@NonNull String lnurl) {
    return new BaseTagFactory(Constants.Tag.LNURL_CODE, lnurl).create();
  }

  /**
   * Create a bolt11 tag.
   *
   * @param bolt11 the BOLT11 invoice
   * @return the created tag
   */
  public static BaseTag createBolt11Tag(@NonNull String bolt11) {
    return new BaseTagFactory(Constants.Tag.BOLT11_CODE, bolt11).create();
  }

  /**
   * Create a preimage tag.
   *
   * @param preimage the payment preimage
   * @return the created tag
   */
  public static BaseTag createPreImageTag(@NonNull String preimage) {
    return new BaseTagFactory(Constants.Tag.PREIMAGE_CODE, preimage).create();
  }

  /**
   * Create a description tag.
   *
   * @param description a human-readable description or escaped JSON
   * @return the created tag
   */
  public static BaseTag createDescriptionTag(@NonNull String description) {
    return new BaseTagFactory(Constants.Tag.DESCRIPTION_CODE, description).create();
  }

  /**
   * Create an amount tag.
   *
   * @param amount the zap amount (typically millisats)
   * @return the created tag
   */
  public static BaseTag createAmountTag(@NonNull Number amount) {
    return new BaseTagFactory(Constants.Tag.AMOUNT_CODE, amount.toString()).create();
  }

  /**
   * Create a tag carrying the zap sender public key.
   *
   * @param publicKey the zap sender public key
   * @return the created tag
   */
  public static BaseTag createZapSenderPubKeyTag(@NonNull PublicKey publicKey) {
    return new BaseTagFactory(Constants.Tag.RECIPIENT_PUBKEY_CODE, publicKey.toString()).create();
  }

  /**
   * Create a zap tag listing receiver and relays, optionally with a weight.
   *
   * @param receiver the zap receiver public key
   * @param relays list of recommended relays
   * @param weight optional splitting weight
   * @return the created tag
   */
  public static BaseTag createZapTag(
      @NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    List<String> params = new ArrayList<>();
    params.add(receiver.toString());
    relays.stream().map(Relay::getUri).forEach(params::add);
    if (weight != null) {
      params.add(weight.toString());
    }
    return BaseTag.create(Constants.Tag.ZAP_CODE, params);
  }

  /**
   * Create a zap tag listing receiver and relays.
   *
   * @param receiver the zap receiver public key
   * @param relays list of recommended relays
   * @return the created tag
   */
  public static BaseTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    return createZapTag(receiver, relays, null);
  }
}
