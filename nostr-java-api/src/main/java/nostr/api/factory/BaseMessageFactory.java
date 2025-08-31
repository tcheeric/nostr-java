/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import lombok.NoArgsConstructor;
import nostr.event.BaseMessage;

/**
 * Base message factory for building protocol messages from inputs.
 *
 * @param <T> message type
 */
@NoArgsConstructor
public abstract class BaseMessageFactory<T extends BaseMessage> {

  /** Build the message instance. */
  public abstract T create();
}
