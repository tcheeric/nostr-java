/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nostr.event;

/**
 *
 * @author squirrel
 */
public enum Marker {
    ROOT("root"),
    REPLY("reply");
    
    private final String value;

    Marker(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
