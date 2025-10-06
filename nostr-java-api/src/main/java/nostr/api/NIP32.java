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
 * NIP-32 helpers (Labeling). Create namespace and label tags.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/32.md">NIP-32</a>
 */
public class NIP32 {

  /**
   * Create a namespace tag for labels (NIP-32).
   *
   * @param namespace the label namespace
   * @return the created namespace tag
   */
  public static BaseTag createNameSpaceTag(@NonNull String namespace) {
    return new BaseTagFactory(Constants.Tag.NAMESPACE_CODE, namespace).create();
  }

  /**
   * Create a label tag within the provided namespace (NIP-32).
   *
   * @param label the label value
   * @param namespace the label's namespace
   * @return the created label tag
   */
  public static BaseTag createLabelTag(@NonNull String label, @NonNull String namespace) {
    return new BaseTagFactory(Constants.Tag.LABEL_CODE, label, namespace).create();
  }
}
