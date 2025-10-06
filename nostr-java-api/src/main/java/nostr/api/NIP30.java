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
 * NIP-30 helpers (Custom emoji). Create emoji tags with shortcode and image URL.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/30.md">NIP-30</a>
 */
public class NIP30 {

  /**
   * Create a custom emoji tag as defined by NIP-30.
   *
   * @param shortcode the emoji shortcode (e.g., "party_parrot")
   * @param imageUrl the URL pointing to the emoji image asset
   * @return the created emoji tag
   */
  public static BaseTag createEmojiTag(@NonNull String shortcode, @NonNull String imageUrl) {
    return new BaseTagFactory(Constants.Tag.EMOJI_CODE, shortcode, imageUrl).create();
  }
}
