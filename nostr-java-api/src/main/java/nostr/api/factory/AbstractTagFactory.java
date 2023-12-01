/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api.factory;

import nostr.base.ITag;

/**
 *
 * @author eric
 * @param <T>
 */
public abstract class AbstractTagFactory<T extends ITag> {

    public abstract T create();
    
}
