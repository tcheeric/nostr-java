/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.NonNull;
import nostr.api.factory.impl.NIP04.DirectMessageEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public class NIP04 extends Nostr {

    /**
     * Create a NIP04 Encrypted Direct Message
     * @param recipient the DM recipient
     * @param content the DM content in clear-text
     * @return the DM event
     */
    public static DirectMessageEvent createDirectMessageEvent(@NonNull PublicKey recipient, @NonNull String content) {
        return new DirectMessageEventFactory(recipient, content).create();
    }
    
    /**
     * Create a NIP04 Encrypted Direct Message
     * @param tags additional note's tags
     * @param recipient the DM recipient
     * @param content the DM content
     * @return the DM event
     */
    public static DirectMessageEvent createDirectMessageEvent(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
        return new DirectMessageEventFactory(tags, recipient, content).create();
    }

    /**
     * Encrypt a DM event
     * @param dm the DM event
     */
    public static void encrypt(@NonNull DirectMessageEvent dm) {
        var identity = Identity.getInstance();
        try {
            identity.encryptDirectMessage(dm);
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Decrypt an encrypted direct message
     * @param dm the encrypted direct message
     * @return the DM content in clear-text
     * @throws NostrException 
     */
    public static String decrypt(@NonNull DirectMessageEvent dm) throws NostrException {
        var identity = Identity.getInstance();
        var recipient = dm.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));
        var rcpt = (PubKeyTag) recipient;
        return identity.decryptDirectMessage(dm.getContent(), rcpt.getPublicKey());

    }
}
