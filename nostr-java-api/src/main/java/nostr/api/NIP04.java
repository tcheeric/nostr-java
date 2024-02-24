/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP04.DirectMessageEventFactory;
import nostr.base.PublicKey;
import nostr.event.BaseTag;
import nostr.event.NIP04Event;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.id.Identity;
import nostr.id.IdentityHelper;
import nostr.util.NostrException;

/**
 * @author eric
 */
@Log
public class NIP04<T extends NIP04Event> extends EventNostr<T> {
	
	public NIP04(@NonNull Identity sender, @NonNull PublicKey recipient) {
		setSender(sender);
		setRecipient(recipient);
	}

    public NIP04<T> createDirectMessageEvent(@NonNull String content) {
        var event = new DirectMessageEventFactory(getRecipient(), content).create();
		this.setEvent((T) event);
        
        return this;
    }

    /**
     * Create a NIP04 Encrypted Direct Message
     *
     * @param tags      additional note's tags
     * @param recipient the DM recipient
     * @param content   the DM content
     * @return the DM event
     */
    public NIP04<T> createDirectMessageEvent(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
    	var event =  new DirectMessageEventFactory(tags, recipient, content).create();
		this.setEvent((T) event);
        
        return this;
    }

    public NIP04<T> encrypt() {
        try {
            new IdentityHelper(getSender()).encryptDirectMessage((DirectMessageEvent) getEvent());
        } catch (NostrException ex) {
            throw new RuntimeException(ex);
        }
        
        return this;
    }

    public static String encrypt(@NonNull IIdentity senderId, @NonNull String message, @NonNull PublicKey recipient) {
        try {
            return new IdentityHelper(senderId).encrypt(message, recipient);
        } catch (NostrException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                 IllegalBlockSizeException | NoSuchPaddingException | InvalidAlgorithmParameterException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Decrypt an encrypted direct message
     *
     * @param rcptId
     * @param dm the encrypted direct message
     * @return the DM content in clear-text
     * @throws NostrException
     */
    public static String decrypt(@NonNull IIdentity rcptId, @NonNull DirectMessageEvent dm) throws NostrException {
        return NIP04.decrypt(rcptId, (GenericEvent) dm);
    }

    public static String decrypt(@NonNull IIdentity rcptId, @NonNull GenericEvent event) throws NostrException {
        var recipient = event.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));
        var pTag = (PubKeyTag) recipient;

        boolean rcptFlag = amITheRecipient(rcptId, event);

        if (!rcptFlag) { // I am the message sender
            log.log(Level.INFO, "I am NOT the recipient of {0}", event);
            log.log(Level.INFO, "The message is being decrypted for {0}", pTag.getPublicKey());
            return new IdentityHelper(rcptId).decryptMessage(event.getContent(), pTag.getPublicKey());
        }

        // I am the message recipient
        var sender = event.getPubKey();
        log.log(Level.INFO, "The message is being decrypted for {0}", sender);
        return new IdentityHelper(rcptId).decryptMessage(event.getContent(), sender);
    }

    public static String decrypt(@NonNull IIdentity identity, @NonNull String encryptedMessage, @NonNull PublicKey recipient) {
        try {
            return new IdentityHelper(identity).decryptMessage(encryptedMessage, recipient);
        } catch (NostrException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean amITheRecipient(@NonNull IIdentity recipient, @NonNull GenericEvent event) {
        var pTag = event.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));

        if (Objects.equals(recipient.getPublicKey(), ((PubKeyTag) pTag).getPublicKey())) {
            return true;
        }

        if (Objects.equals(recipient.getPublicKey(), event.getPubKey())) {
            return false;
        }

        throw new RuntimeException("Unrelated event");
    }
}
