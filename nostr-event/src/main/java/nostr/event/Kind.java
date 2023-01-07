/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event;

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
    REPLACEABLE_EVENT(10_000, "replaceable_event"),
    EPHEMEREAL_EVENT(20_000, "ephemereal_event"),
    UNDEFINED(-1, "undefined");
            
    private final int value;    
    private final String name;

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
    
    public static Kind valueOf(int value) {
        switch(value) {
            case 0 -> {
                return SET_METADATA;
            }
            case 1 -> {
                return TEXT_NOTE;
            } 
            case 2 -> {
                return RECOMMEND_SERVER;
            }
            case 3 -> {
                return CONTACT_LIST;
            }
            case 4 -> {
                return ENCRYPTED_DIRECT_MESSAGE;
            }
            case 5 -> {
                return DELETION;
            }
            case 7 -> {
                return REACTION;
            }
            case 10_000 -> {
                return REPLACEABLE_EVENT;
            }
            case 20_000 -> {
                return EPHEMEREAL_EVENT;
            }
        }
        return UNDEFINED;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
