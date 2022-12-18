/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.tcheeric.nostr.base;

import java.util.List;

/**
 *
 * @author squirrel
 * @param <T>
 */
public interface INostrList<T> extends IElement {

    public abstract void add(T elt);

    public abstract void addAll(INostrList<T> list);

    public abstract List<T> getList();
    
    public abstract int size();
}
