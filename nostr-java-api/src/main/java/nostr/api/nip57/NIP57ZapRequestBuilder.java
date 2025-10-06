package nostr.api.nip57;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.nip01.NIP01TagFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.ZapRequest;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;

/**
 * Builds zap request events for {@link nostr.api.NIP57}.
 */
public final class NIP57ZapRequestBuilder {

  private Identity defaultSender;

  public NIP57ZapRequestBuilder(Identity defaultSender) {
    this.defaultSender = defaultSender;
  }

  public void updateDefaultSender(Identity defaultSender) {
    this.defaultSender = defaultSender;
  }

  public GenericEvent buildFromZapRequest(
      @NonNull Identity sender,
      @NonNull ZapRequest zapRequest,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    GenericEvent genericEvent = initialiseZapRequest(sender, content);
    populateCommonZapRequestTags(
        genericEvent,
        zapRequest.getRelaysTag(),
        zapRequest.getAmount(),
        zapRequest.getLnUrl(),
        recipientPubKey,
        zappedEvent,
        addressTag);
    return genericEvent;
  }

  public GenericEvent buildFromZapRequest(
      @NonNull ZapRequest zapRequest,
      @NonNull String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    return buildFromZapRequest(resolveSender(null), zapRequest, content, recipientPubKey, zappedEvent, addressTag);
  }

  public GenericEvent buildFromParameters(
      Long amount,
      String lnUrl,
      BaseTag relaysTag,
      String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    if (!(relaysTag instanceof RelaysTag)) {
      throw new IllegalArgumentException("tag must be of type RelaysTag");
    }
    GenericEvent genericEvent = initialiseZapRequest(resolveSender(null), content);
    populateCommonZapRequestTags(
        genericEvent, (RelaysTag) relaysTag, amount, lnUrl, recipientPubKey, zappedEvent, addressTag);
    return genericEvent;
  }

  public GenericEvent buildFromParameters(
      Long amount,
      String lnUrl,
      List<Relay> relays,
      String content,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    return buildFromParameters(
        amount, lnUrl, new RelaysTag(relays), content, recipientPubKey, zappedEvent, addressTag);
  }

  public GenericEvent buildSimpleZapRequest(
      Long amount,
      String lnUrl,
      List<String> relays,
      String content,
      PublicKey recipientPubKey) {
    return buildFromParameters(
        amount,
        lnUrl,
        new RelaysTag(relays.stream().map(Relay::new).toList()),
        content,
        recipientPubKey,
        null,
        null);
  }

  private GenericEvent initialiseZapRequest(Identity sender, String content) {
    Identity resolved = resolveSender(sender);
    GenericEventFactory factory =
        new GenericEventFactory(resolved, Constants.Kind.ZAP_REQUEST, content == null ? "" : content);
    return factory.create();
  }

  private void populateCommonZapRequestTags(
      GenericEvent event,
      RelaysTag relaysTag,
      Number amount,
      String lnUrl,
      PublicKey recipientPubKey,
      GenericEvent zappedEvent,
      BaseTag addressTag) {
    event.addTag(relaysTag);
    event.addTag(NIP57TagFactory.amount(amount));
    event.addTag(NIP57TagFactory.lnurl(lnUrl));

    if (recipientPubKey != null) {
      event.addTag(NIP01TagFactory.pubKeyTag(recipientPubKey));
    }
    if (zappedEvent != null) {
      event.addTag(NIP01TagFactory.eventTag(zappedEvent.getId()));
    }
    if (addressTag != null) {
      if (!Constants.Tag.ADDRESS_CODE.equals(addressTag.getCode())) {
        throw new IllegalArgumentException("tag must be of type AddressTag");
      }
      event.addTag(addressTag);
    }
  }

  private Identity resolveSender(Identity override) {
    Identity resolved = override != null ? override : defaultSender;
    if (resolved == null) {
      throw new IllegalStateException("Sender identity is required to build zap requests");
    }
    return resolved;
  }
}
