/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

public class NIP30 {

  /**
   * @param shortcode
   * @param imageUrl
   */
  public static BaseTag createEmojiTag(@NonNull String shortcode, @NonNull String imageUrl) {
    return new BaseTagFactory(Constants.Tag.EMOJI_CODE, shortcode, imageUrl).create();
  }
}
