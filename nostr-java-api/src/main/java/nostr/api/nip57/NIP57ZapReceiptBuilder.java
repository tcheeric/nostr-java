package nostr.api.nip57;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.nip01.NIP01TagFactory;
import nostr.base.Kind;
import nostr.base.PublicKey;
import nostr.base.json.EventJsonMapper;
import nostr.event.filter.Filterable;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.EventEncodingException;
import nostr.event.tag.AddressTag;
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
      // Store description (escaped) and include description_hash for validation
      receipt.addTag(NIP57TagFactory.description(StringEscapeUtils.escapeJson(description)));
      var hash = nostr.util.NostrUtil.bytesToHex(nostr.util.NostrUtil.sha256(description.getBytes()));
      receipt.addTag(NIP57TagFactory.descriptionHash(hash));
    } catch (JsonProcessingException ex) {
      throw new EventEncodingException("Failed to encode zap receipt description", ex);
    } catch (java.security.NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    }
    receipt.addTag(NIP57TagFactory.bolt11(bolt11));
    receipt.addTag(NIP57TagFactory.preimage(preimage));
    receipt.addTag(NIP57TagFactory.zapSender(zapRequestEvent.getPubKey()));
    receipt.addTag(NIP01TagFactory.eventTag(zapRequestEvent.getId()));

    Filterable.getTypeSpecificTags(AddressTag.class, zapRequestEvent)
        .stream()
        .findFirst()
        .ifPresent(receipt::addTag);

    // Validate invoice amount when available (best-effort)
    try {
      long invoiceMsat = Bolt11Util.parseMsat(bolt11);
      if (invoiceMsat >= 0) {
        var amountTag =
            nostr.event.filter.Filterable.requireTagOfTypeWithCode(
                nostr.event.tag.GenericTag.class, nostr.config.Constants.Tag.AMOUNT_CODE, zapRequestEvent);
        String amountStr = amountTag.getAttributes().get(0).value().toString();
        long requestedMsat = Long.parseLong(amountStr);
        if (requestedMsat != invoiceMsat) {
          throw new IllegalArgumentException(
              "Invoice amount does not match zap request amount: requested="
                  + requestedMsat
                  + " msat, invoice="
                  + invoiceMsat
                  + " msat");
        }
      }
    } catch (RuntimeException ex) {
      // Preserve existing behavior for now: do not fail if amount tag is missing
      // or invoice lacks amount; only propagate strict mismatches and parsing errors.
      if (ex instanceof IllegalArgumentException) {
        throw ex;
      }
    }

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
