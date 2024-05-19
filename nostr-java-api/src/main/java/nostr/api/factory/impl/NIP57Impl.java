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
import nostr.event.impl.ZapRequestEvent;
import nostr.event.impl.ZapRequestEvent.ZapRequest;
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

//  @Data
//  @EqualsAndHashCode(callSuper = false)
//  public static class ZapResponseEventFactory extends EventFactory<ZapRequestEvent> {
//    public ZapResponseEventFactory(@NonNull Identity sender, List<BaseTag> tags, @NonNull String content) {
//      super(sender, tags, content);
//    }
//
//    @Override
//    public ZapResponseEvent create() {
//      var event = new ZapRequestEvent(getSender(), getTags(), getContent());
//      getTags().forEach(event::addTag);
//      return event;
//    }
//  }
}
