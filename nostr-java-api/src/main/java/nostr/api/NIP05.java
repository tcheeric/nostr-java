/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import nostr.api.factory.impl.NIP05.InternetIdentifierMetadataEventFactory;
import nostr.base.UserProfile;
import nostr.event.impl.InternetIdentifierMetadataEvent;

/**
 *
 * @author eric
 */
public class NIP05 {
 
    /**
     * Create an Internet Identifier Metadata (IIM) Event
     * @param profile the associate user profile
     * @return the IIM event
     */
    public static InternetIdentifierMetadataEvent createInternetIdentifierMetadataEvent(UserProfile profile) {
        return new InternetIdentifierMetadataEventFactory(profile).create();
    }
}
