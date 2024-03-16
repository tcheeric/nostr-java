/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.impl.NIP33.AddressTagFactory;
import nostr.api.factory.impl.NIP33.IdentifierTagFactory;
import nostr.api.factory.impl.NIP33.ParameterizedReplaceableEventFactory;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.ParameterizedReplaceableEvent;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;

import java.util.List;

/**
 * @author eric
 */
public class NIP33 extends Nostr {

  /**
   * @param kind
   * @param comment
   * @return
   */
  public static ParameterizedReplaceableEvent createParameterizedReplaceableEvent(@NonNull Integer kind, String comment) {
    return new ParameterizedReplaceableEventFactory(kind, comment).create();
  }

  /**
   * @param tags
   * @param kind
   * @param comment
   * @return
   */
  public static ParameterizedReplaceableEvent createParameterizedReplaceableEvent(@NonNull List<BaseTag> tags, @NonNull Integer kind, String comment) {
    return new ParameterizedReplaceableEventFactory(tags, kind, comment).create();
  }

  /**
   * @param id
   * @return
   */
  public static IdentifierTag createIdentifierTag(@NonNull String id) {
    return new IdentifierTagFactory(id).create();
  }

  /**
   * @param kind
   * @param publicKey
   * @param idTag
   * @param relay
   * @return
   */
  public static AddressTag createAddressTag(@NonNull Integer kind, @NonNull PublicKey publicKey, @NonNull IdentifierTag idTag, Relay relay) {
    var result = new AddressTagFactory(publicKey).create();
    result.setIdentifierTag(idTag);
    result.setKind(kind);
    result.setRelay(relay);
    return result;
  }
}
