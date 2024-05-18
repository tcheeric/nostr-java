/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.factory.impl.NIP57Impl.ZapRequestEventFactory;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.NIP57Event;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.impl.ZapRequestEvent.ZapRequest;
import nostr.event.tag.EventTag;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP57<T extends NIP57Event> extends EventNostr<T> {


  private static final int ZAP_REQUEST_EVENT_KIND = 9734;
  private static final int ZAP_RECEIPT_EVENT_KIND = 9735;
  private static final String LNURL_TAG_NAME = "lnurl";
  private static final String BOLT11_TAG_NAME = "bolt11";
  private static final String PREIMAGE_TAG_NAME = "preimage";
  private static final String DESCRIPTION_TAG_NAME = "description";
  private static final String AMOUNT_TAG_NAME = "amount";
  private static final String ZAP_TAG_NAME = "zap";
  private static final String RELAYS_TAG_NAME = "relays";

  public NIP57(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * @param content
   */
  public NIP57<T> createZapRequestEvent(@NonNull List<BaseTag> baseTags, String content, @NonNull ZapRequest zapRequest) {
    setEvent((T) new ZapRequestEventFactory(getSender(), baseTags, content, zapRequest).create());
    return this;
  }

  public NIP57<T> createZapRequestEvent(List<BaseTag> baseTags, @NonNull String content, @NonNull Integer amount, @NonNull String lnurl, @NonNull PublicKey recipient, @NonNull RelaysTag relays) {
    return createZapRequestEvent(baseTags, content, new ZapRequest(relays, amount, lnurl, recipient));
  }

  /**
   *
   */
  public NIP57<T> createZapReceiptEvent() {
    var factory = new GenericEventFactory(getSender(), ZAP_RECEIPT_EVENT_KIND, "");
    var event = factory.create();
    setEvent((T) event);

    return this;
  }

  /**
   * @param zapEvent
   * @param bolt11
   * @param preimage
   * @param recipient
   * @param eventTag
   * @return
   */
  public NIP57<T> createZapReceiptEvent(@NonNull GenericEvent zapEvent, @NonNull String bolt11, @NonNull String preimage, @NonNull PublicKey recipient, @NonNull EventTag eventTag) {
    var factory = new GenericEventFactory(getSender(), ZAP_RECEIPT_EVENT_KIND, "");
    var event = factory.create();
    setEvent((T) event);

    return this.addBolt11Tag(bolt11)
        .addDescriptionTag(Nostr.Json.encode(zapEvent))
        .addPreImageTag(preimage)
        .addRecipientTag(recipient)
        .addEventTag(eventTag);
  }

  public NIP57<T> addLnurlTag(@NonNull String lnurl) {
    getEvent().addTag(createLnurlTag(lnurl));
    return this;
  }

  public NIP57<T> addEventTag(@NonNull EventTag tag) {
    getEvent().addTag(tag);
    return this;
  }

  public NIP57<T> addBolt11Tag(@NonNull String bolt11) {
    getEvent().addTag(createBolt11Tag(bolt11));
    return this;
  }

  public NIP57<T> addPreImageTag(@NonNull String preimage) {
    getEvent().addTag(createPreImageTag(preimage));
    return this;
  }

  public NIP57<T> addDescriptionTag(@NonNull String description) {
    getEvent().addTag(createDescriptionTag(description));
    return this;
  }

  public NIP57<T> addAmountTag(@NonNull Integer amount) {
    getEvent().addTag(createAmountTag(amount));
    return this;
  }

  public NIP57<T> addRecipientTag(@NonNull PublicKey recipient) {
    getEvent().addTag(NIP01.createPubKeyTag(recipient));
    return this;
  }

  public NIP57<T> addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    getEvent().addTag(createZapTag(receiver, relays, weight));
    return this;
  }

  public NIP57<T> addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    getEvent().addTag(createZapTag(receiver, relays));
    return this;
  }

  public NIP57<T> addRelayTag(@NonNull Relay relay) {
    getEvent().addTag(NIP42.createRelayTag(relay));
    return this;
  }

  public NIP57<T> addRelayTags(@NonNull List<Relay> relays) {
    relays.forEach(this::addRelayTag);
    return this;
  }

  /**
   * @param lnurl
   * @return
   */
  public static GenericTag createLnurlTag(@NonNull String lnurl) {
    return new TagFactory(LNURL_TAG_NAME, 57, lnurl).create();
  }

  /**
   * @param bolt11
   * @return
   */
  public static GenericTag createBolt11Tag(@NonNull String bolt11) {
    return new TagFactory(BOLT11_TAG_NAME, 57, bolt11).create();
  }

  /**
   * @param preimage
   */
  public static GenericTag createPreImageTag(@NonNull String preimage) {
    return new TagFactory(PREIMAGE_TAG_NAME, 57, preimage).create();
  }

  /**
   * @param description
   */
  public static GenericTag createDescriptionTag(@NonNull String description) {
    return new TagFactory(DESCRIPTION_TAG_NAME, 57, description).create();
  }

  /**
   * @param amount
   */
  public static GenericTag createAmountTag(@NonNull Integer amount) {
    return new TagFactory(AMOUNT_TAG_NAME, 57, amount.toString()).create();
  }

  /**
   * @param relayUri
   */
  public static GenericTag createRelaysTag(@NonNull String relayUri) {
    return new TagFactory(RELAYS_TAG_NAME, 57, relayUri).create();
  }

  /**
   * @param receiver
   * @param relays
   * @param weight
   */
  public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
    List<ElementAttribute> attributes = new ArrayList<>();
    var receiverAttr = new ElementAttribute("receiver", receiver.toString(), 57);
    var relayAttrs = relays.stream().map(relay -> new ElementAttribute("relay", relay.getUri(), 57)).toList();
    if (weight != null) {
      var weightAttr = new ElementAttribute("weight", weight, 57);
      attributes.add(weightAttr);
    }

    attributes.add(receiverAttr);
    attributes.addAll(relayAttrs);
    return new GenericTag(ZAP_TAG_NAME, 57, attributes);
  }

  /**
   * @param receiver
   * @param relays
   */
  public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
    return createZapTag(receiver, relays, null);
  }
}
