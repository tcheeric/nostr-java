package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

import java.util.List;

/**
 * NIP-14 helpers (Subject tag in text notes). Create subject tags for threads.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/14.md">NIP-14</a>
 */
public class NIP14 {

  /**
   * Create a subject tag
   *
   * @param subject the subject
   * @return the created subject tag
   */
  public static BaseTag createSubjectTag(@NonNull String subject) {
    return new BaseTagFactory(Constants.Tag.SUBJECT_CODE, List.of(subject)).create();
  }
}
