/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

/**
 * @author eric
 */
public class NIP03 extends EventNostr {

  public NIP03(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create a NIP03 OTS event
   *
   * @param referencedEvent the referenced event
   * @param ots the full content of an .ots file containing at least one Bitcoin attestation
   * @param alt the note's content
   * @return an OTS event
   */
  public NIP03 createOtsEvent(
      @NonNull GenericEvent referencedEvent, @NonNull String ots, @NonNull String alt) {
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.OTS_ATTESTATION, ots).create();
    genericEvent.addTag(NIP31.createAltTag(alt));
    genericEvent.addTag(NIP01.createEventTag(referencedEvent.getId()));
    this.updateEvent(genericEvent);

    return this;
  }
}
