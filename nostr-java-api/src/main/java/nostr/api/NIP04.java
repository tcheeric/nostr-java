/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP04.DirectMessageEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.id.Identity;
import nostr.id.IdentityHelper;
import nostr.util.NostrException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author eric
 */
@Log
public class NIP04 extends Nostr {

    /**
     * Create a NIP04 Encrypted Direct Message
     *
     * @param recipient the DM recipient
     * @param content   the DM content in clear-text
     * @return the DM event
     */
    public static DirectMessageEvent createDirectMessageEvent(@NonNull PublicKey recipient, @NonNull String content) {
        return new DirectMessageEventFactory(recipient, content).create();
    }

    /**
     * Create a NIP04 Encrypted Direct Message
     *
     * @param tags      additional note's tags
     * @param recipient the DM recipient
     * @param content   the DM content
     * @return the DM event
     */
    public static DirectMessageEvent createDirectMessageEvent(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
        return new DirectMessageEventFactory(tags, recipient, content).create();
    }

    /**
     * Encrypt a DM event
     *
     * @param dm the DM event
     */
    public static void encrypt(@NonNull IIdentity identity, @NonNull DirectMessageEvent dm) {
        try {
            new IdentityHelper(identity).encryptDirectMessage(dm);
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String encrypt(@NonNull IIdentity identity, @NonNull String message, @NonNull PublicKey recipient) {
        try {
            return new IdentityHelper(identity).encrypt(message, recipient);
        } catch (NostrException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | NoSuchPaddingException | InvalidAlgorithmParameterException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Decrypt an encrypted direct message
     *
     * @param dm the encrypted direct message
     * @return the DM content in clear-text
     * @throws NostrException
     */
    public static String decrypt(@NonNull IIdentity identity, @NonNull DirectMessageEvent dm) throws NostrException {
        return NIP04.decrypt(identity, (GenericEvent) dm);
    }

    public static String decrypt(@NonNull IIdentity identity, @NonNull GenericEvent event) throws NostrException {
        var recipient = event.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));
        var pTag = (PubKeyTag) recipient;

        boolean rcptFlag = amITheRecipient(identity, event);

        if (!rcptFlag) { // I am the message sender
            log.log(Level.INFO, "I am NOT the recipient of {0}", event);
            log.log(Level.INFO, "The message is being decrypted for {0}", pTag.getPublicKey());
            return new IdentityHelper(identity).decryptMessage(event.getContent(), pTag.getPublicKey());
        }

        // I am the message recipient
        var sender = event.getPubKey();
        log.log(Level.INFO, "The message is being decrypted for {0}", sender);
        return new IdentityHelper(identity).decryptMessage(event.getContent(), sender);
    }

    public static String decrypt(@NonNull IIdentity identity, @NonNull String encryptedMessage, @NonNull PublicKey recipient) {
        try {
            return new IdentityHelper(identity).decryptMessage(encryptedMessage, recipient);
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean amITheRecipient(@NonNull IIdentity identity, @NonNull GenericEvent event) {
        var pTag = event.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));

        if (Objects.equals(identity.getPublicKey(), ((PubKeyTag) pTag).getPublicKey())) {
            return true;
        }

        if (Objects.equals(identity.getPublicKey(), event.getPubKey())) {
            return false;
        }

        throw new RuntimeException("Unrelated event");
    }
}
