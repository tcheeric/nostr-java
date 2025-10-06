/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.event.impl.GenericEvent;
import nostr.event.message.OkMessage;

/**
 * NIP-20 helpers (OK message). Build OK messages indicating relay acceptance/rejection.
 * Spec: <a href="https://github.com/nostr-protocol/nips/blob/master/20.md">NIP-20</a>
 */
public class NIP20 {

  /**
   * Create an OK message providing information about if an event was accepted or rejected.
   *
   * @param event the related event
   * @param flag true if the relay accepted the event; false otherwise
   * @param message additional information as to why the command succeeded or failed
   * @return the OK message
   */
  public static OkMessage createOkMessage(
      @NonNull GenericEvent event, boolean flag, String message) {
    return new OkMessage(event.getId(), flag, message);
  }
}
