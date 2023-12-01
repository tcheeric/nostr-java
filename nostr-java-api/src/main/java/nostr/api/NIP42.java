/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import lombok.NonNull;
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

    /**
     * 
     * @param challenge
     * @param relay
     * @return 
     */
    public static ClientAuthenticationEvent createClientAuthenticationEvent(@NonNull String challenge, @NonNull Relay relay) {
        return new ClientAuthenticationEventFactory(challenge, relay).create();
    }
    
    /**
     * 
     * @param tags
     * @param challenge
     * @param relay
     * @return 
     */
    public static ClientAuthenticationEvent createClientAuthenticationEvent(@NonNull List<BaseTag> tags, @NonNull String challenge, @NonNull Relay relay) {
        return new ClientAuthenticationEventFactory(tags, challenge, relay).create();
    }
    
    /**
     * 
     * @param relay
     * @return 
     */
    public static GenericTag createRelayTag(@NonNull Relay relay) {
        return new RelaysTagFactory(relay).create();
    }
    
    /**
     * 
     * @param challenge
     * @return 
     */
    public static GenericTag createChallengeTag(@NonNull String challenge) {
        return new ChallengeTagFactory(challenge).create();
    }
    
    /**
     * 
     * @param event
     * @return 
     */
    public static ClientAuthenticationMessage createClientAuthenticationMessage(@NonNull ClientAuthenticationEvent event) {
        return new ClientAuthenticationMessageFactory(event).create();
    }
    
    /**
     * 
     * @param challenge
     * @return 
     */
    public static GenericMessage createRelayAuthenticationMessage(@NonNull String challenge) {
        return new RelayAuthenticationMessageFactory(challenge).create();
    }

    /**
     * 
     * @param challenge
     * @param relay
     * @throws NostrException 
     */
    public static void auth(@NonNull String challenge, @NonNull Relay relay) throws NostrException {
        Client client = Nostr.createClient();
        client.auth(challenge, relay);
    }

}
