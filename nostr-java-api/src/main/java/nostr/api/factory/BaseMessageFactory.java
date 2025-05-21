/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import lombok.NoArgsConstructor;
import nostr.event.BaseMessage;
import nostr.event.message.GenericMessage;

/**
 *
 * @author eric
 * @param <T>
 */
@NoArgsConstructor
public abstract class BaseMessageFactory<T extends BaseMessage> {

    public abstract T create();
}
