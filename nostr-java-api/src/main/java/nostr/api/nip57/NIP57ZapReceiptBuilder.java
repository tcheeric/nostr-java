package nostr.api.nip57;

import nostr.base.json.EventJsonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.nip01.NIP01TagFactory;
import nostr.base.IEvent;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.event.filter.Filterable;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.AddressTag;
import nostr.event.json.codec.EventEncodingException;
import nostr.id.Identity;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Builds zap receipt events for {@link nostr.api.NIP57}.
 */
public final class NIP57ZapReceiptBuilder {

  private Identity defaultSender;

  public NIP57ZapReceiptBuilder(Identity defaultSender) {
    this.defaultSender = defaultSender;
  }

  public void updateDefaultSender(Identity defaultSender) {
    this.defaultSender = defaultSender;
  }

  public GenericEvent build(
      @NonNull GenericEvent zapRequestEvent,
      @NonNull String bolt11,
      @NonNull String preimage,
      @NonNull PublicKey zapRecipient) {
    GenericEvent receipt =
        new GenericEventFactory(resolveSender(null), Kind.ZAP_RECEIPT.getValue(), "").create();

    receipt.addTag(NIP01TagFactory.pubKeyTag(zapRecipient));
    try {
      String description = EventJsonMapper.mapper().writeValueAsString(zapRequestEvent);
      receipt.addTag(NIP57TagFactory.description(StringEscapeUtils.escapeJson(description)));
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to encode zap receipt description", ex);
    }
    receipt.addTag(NIP57TagFactory.bolt11(bolt11));
    receipt.addTag(NIP57TagFactory.preimage(preimage));
    receipt.addTag(NIP57TagFactory.zapSender(zapRequestEvent.getPubKey()));
    receipt.addTag(NIP01TagFactory.eventTag(zapRequestEvent.getId()));

    Filterable.getTypeSpecificTags(AddressTag.class, zapRequestEvent)
        .stream()
        .findFirst()
        .ifPresent(receipt::addTag);

    receipt.setCreatedAt(zapRequestEvent.getCreatedAt());
    return receipt;
  }

  private Identity resolveSender(Identity override) {
    Identity resolved = override != null ? override : defaultSender;
    if (resolved == null) {
      throw new IllegalStateException("Sender identity is required to build zap receipts");
    }
    return resolved;
  }
}
