/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

/**
 * NIP-40 helpers (Expiration). Create expiration tags for events.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/40.md">NIP-40</a>
 */
public class NIP40 {

  /**
   * Create an expiration tag (NIP-40) to indicate when an event should be considered expired.
   *
   * @param expiration unix timestamp (seconds) when the event expires
   * @return the created expiration tag
   */
  public static BaseTag createExpirationTag(@NonNull Integer expiration) {
    return new BaseTagFactory(Constants.Tag.EXPIRATION_CODE, expiration.toString()).create();
  }
}
