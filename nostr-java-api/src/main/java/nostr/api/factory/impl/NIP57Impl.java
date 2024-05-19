/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import nostr.api.factory.EventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.ZapReceiptEvent;
import nostr.event.impl.ZapReceiptEvent.ZapReceipt;
import nostr.event.impl.ZapRequestEvent;
import nostr.event.impl.ZapRequestEvent.ZapRequest;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;

import java.util.List;

public class NIP57Impl {

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ZapRequestEventFactory extends EventFactory<ZapRequestEvent> {
    private final ZapRequest zapRequest;
    private final PublicKey recipientKey;

    public ZapRequestEventFactory(@NonNull Identity sender, @NonNull PublicKey recipientPubKey, List<BaseTag> tags, String content, @NonNull ZapRequest zapRequest) {
      super(sender, tags, content);
      this.zapRequest = zapRequest;
      this.recipientKey = recipientPubKey;
    }

    public ZapRequestEventFactory(@NonNull Identity sender, @NonNull PublicKey recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull List<String> relaysTags) {
      this(sender, recipientPubKey, tags, content, new ZapRequest(new RelaysTag(relaysTags), amount, lnUrl));
    }

    public ZapRequestEventFactory(@NonNull Identity sender, @NonNull PublicKey recipientPubKey, List<BaseTag> tags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull String... relaysTags) {
      this(sender, recipientPubKey, tags, content, amount, lnUrl, List.of(relaysTags));
    }

    @Override
    public ZapRequestEvent create() {
      return new ZapRequestEvent(getSender(), recipientKey, getTags(), getContent(), zapRequest);
    }
  }

  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class ZapReceiptEventFactory extends EventFactory<ZapReceiptEvent> {
    private final ZapReceipt zapReceipt;
    private final PubKeyTag zapRequestPubKeyTag;
    private final EventTag zapRequestEventTag;
    private final AddressTag zapRequestAddressTag;

    public ZapReceiptEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull PubKeyTag zapRequestPubKeyTag, EventTag zapRequestEventTag, AddressTag zapRequestAddressTag, ZapReceipt zapReceipt) {
      super(sender, tags, "");
      this.zapReceipt = zapReceipt;
      this.zapRequestPubKeyTag = zapRequestPubKeyTag;
      this.zapRequestEventTag = zapRequestEventTag;
      this.zapRequestAddressTag = zapRequestAddressTag;
    }

    public ZapReceiptEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull PubKeyTag zapRequestPubKeyTag, EventTag zapRequestEventTag, AddressTag zapRequestAddressTag, @NonNull String bolt11,
        @NonNull String descriptionSha256, @NonNull String preimage) {
      this(sender, tags, zapRequestPubKeyTag, zapRequestEventTag, zapRequestAddressTag, new ZapReceipt(bolt11, descriptionSha256, preimage));
    }

    @Override
    public ZapReceiptEvent create() {
      return new ZapReceiptEvent(getSender(), zapRequestPubKeyTag, zapRequestEventTag, zapRequestAddressTag, zapReceipt);
    }
  }
}
