package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

/**
 * NIP-31 helpers (Alt tag). Create alt tags describing event context/purpose.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/31.md
 */
public class NIP31 {

  /**
   * Create an alt tag describing the purpose or context of an event (NIP-31).
   *
   * @param alt the human-friendly alternative description
   * @return the created alt tag
   */
  public static BaseTag createAltTag(@NonNull String alt) {
    return new BaseTagFactory(Constants.Tag.ALT_CODE, alt).create();
  }
}
