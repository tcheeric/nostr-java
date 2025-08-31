/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.config.Constants;
import nostr.event.BaseTag;

/**
 * NIP-14 helpers (Subject tag in text notes). Create subject tags for threads.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/14.md
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
