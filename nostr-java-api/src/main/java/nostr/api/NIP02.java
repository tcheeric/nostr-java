/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.PublicKey;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * NIP-02 helpers (Contact List). Create and manage kind 3 contact lists and p-tags.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/02.md
 */
public class NIP02 extends EventNostr {

  public NIP02(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a contact list event (kind 3) as defined by NIP-02.
   *
   * @param pubKeyTags the list of {@code p} tags representing contacts and optional relay/petname
   * @return this instance for chaining
   */
  @SuppressWarnings("rawtypes")
  public NIP02 createContactListEvent(List<BaseTag> pubKeyTags) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.CONTACT_LIST, pubKeyTags, "").create();
    updateEvent(genericEvent);
    return this;
  }

  /**
   * Add a pubkey tag to the contact list event
   *
   * @param tag the pubkey tag
   */
  public NIP02 addContactTag(@NonNull BaseTag tag) {
    if (!tag.getCode().equals(Constants.Tag.PUBKEY_CODE)) {
      throw new IllegalArgumentException("Tag must be a pubkey tag");
    }
    getEvent().addTag(tag);
    return this;
  }

  /**
   * Add a pubkey tag to the contact list event
   *
   * @param publicKey the public key to add to the contact list
   */
  public NIP02 addContactTag(@NonNull PublicKey publicKey) {
    return addContactTag(NIP01.createPubKeyTag(publicKey));
  }
}
