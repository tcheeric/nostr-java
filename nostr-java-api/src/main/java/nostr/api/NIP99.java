/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP99Impl;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.NIP99Event;
import nostr.id.IIdentity;

import java.util.List;

public class NIP99<T extends NIP99Event> extends EventNostr<T> {
  public NIP99(@NonNull IIdentity sender) {
    setSender(sender);
  }

  public NIP99<T> createClassifiedListingEvent(PublicKey pubKey, List<BaseTag> tags, String content, String title, String summary, String location, @NonNull List<String> price, String currency) {
    var event = new NIP99Impl.ClassifiedListingEventFactory(getSender(), tags, content, title, summary, location, price, currency).create();
    this.setEvent((T) event);

    return this;
  }
}
