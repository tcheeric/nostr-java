/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import nostr.api.factory.impl.NIP42.ChallengeTagFactory;
import nostr.api.factory.impl.NIP42.ClientAuthenticationEventFactory;
import nostr.api.factory.impl.NIP42.ClientAuthenticationMessageFactory;
import nostr.api.factory.impl.NIP42.RelayAuthenticationMessageFactory;
import nostr.api.factory.impl.NIP42.RelaysTagFactory;
import nostr.base.Relay;
import nostr.client.Client;
import nostr.event.BaseTag;
import nostr.event.impl.ClientAuthenticationEvent;
import nostr.event.impl.GenericMessage;
import nostr.event.impl.GenericTag;
import nostr.event.message.ClientAuthenticationMessage;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP42 extends Nostr {

    public static ClientAuthenticationEvent createClientAuthenticationEvent(String challenge, Relay relay) {
        return new ClientAuthenticationEventFactory(challenge, relay).create();
    }
    
    public static ClientAuthenticationEvent createClientAuthenticationEvent(List<BaseTag> tags, String challenge, Relay relay) {
        return new ClientAuthenticationEventFactory(tags, challenge, relay).create();
    }
    
    public static GenericTag createRelayTag(Relay relay) {
        return new RelaysTagFactory(relay).create();
    }
    
    public static GenericTag createChallengeTag(String challenge) {
        return new ChallengeTagFactory(challenge).create();
    }
    
    public static ClientAuthenticationMessage createClientAuthenticationMessage(ClientAuthenticationEvent event) {
        return new ClientAuthenticationMessageFactory(event).create();
    }
    
    public static GenericMessage createRelayAuthenticationMessage(String challenge) {
        return new RelayAuthenticationMessageFactory(challenge).create();
    }

    public static void auth(String challenge, Relay relay) throws NostrException {
        Client client = Nostr.createClient();
        client.auth(challenge, relay);
    }

}
