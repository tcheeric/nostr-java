/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcheeric.nostr.event;

import lombok.AllArgsConstructor;

/**
 *
 * @author squirrel
 */
@AllArgsConstructor
public enum Kind {
    SET_METADATA(0, "set_metadata"),
    TEXT_NOTE(1, "text_note"),
    RECOMMEND_SERVER(2, "recommend_server"),
    CONTACT_LIST(3, "contact_list"),
    ENCRYPTED_DIRECT_MESSAGE(4, "encrypted_direct_message"),
    DELETION(5, "deletion"),
    REACTION(7, "reaction"),
    REPLACEABLE_EVENT(10000, "replaceable_event"),
    EPHEMEREAL_EVENT(20000, "ephemereal_event");
            
    private final int value;    
    private final String name;

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
