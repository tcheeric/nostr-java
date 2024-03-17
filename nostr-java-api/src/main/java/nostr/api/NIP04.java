/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP04Impl.DirectMessageEventFactory;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.nip04.MessageCipher04;
import nostr.event.BaseTag;
import nostr.event.NIP04Event;
import nostr.event.impl.DirectMessageEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.util.NostrException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author eric
 */
@Log
@Deprecated(since = "NIP-44")
public class NIP04<T extends NIP04Event> extends EventNostr<T> {

    public NIP04(@NonNull IIdentity sender, @NonNull PublicKey recipient) {
        setSender(sender);
        setRecipient(recipient);
    }

    /**
     * Create a NIP04 Encrypted Direct Message
     * @param content the DM content in clear-text
     */
    public NIP04<T> createDirectMessageEvent(@NonNull String content) {
        var encryptedContent = encrypt(getSender(), content, getRecipient());
        var event = new DirectMessageEventFactory(getSender(), getRecipient(), encryptedContent).create();
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
        var encryptedContent = encrypt(getSender(), content, recipient);
        var event = new DirectMessageEventFactory(tags, recipient, encryptedContent).create();
        this.setEvent((T) event);

        return this;
    }

    /**
     * Encrypt the direct message
     * @return the current instance with an encrypted message
     */
    private NIP04<T> encrypt() {
        encryptDirectMessage(getSender(), (DirectMessageEvent) getEvent());
        return this;
    }

    /**
     *
     * @param senderId the sender identity
     * @param message the message to be encrypted
     * @param recipient the recipient public key
     * @return the encrypted message
     */
    public static String encrypt(@NonNull IIdentity senderId, @NonNull String message, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher04(senderId.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.encrypt(message);
    }

    /**
     * Decrypt an encrypted direct message
     *
     * @param rcptId
     * @param dm     the encrypted direct message
     * @return the DM content in clear-text
     * @throws NostrException
     */
    public static String decrypt(@NonNull IIdentity rcptId, @NonNull DirectMessageEvent dm) throws NostrException {
        return NIP04.decrypt(rcptId, (GenericEvent) dm);
    }

    /**
     * Decrypt an encrypted direct message
     * @param identity the sender identity
     * @param encryptedMessage the encrypted message
     * @param recipient the recipient public key
     * @return the DM content in clear-text
     */
    public static String decrypt(@NonNull IIdentity identity, @NonNull String encryptedMessage, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher04(identity.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.decrypt(encryptedMessage);
    }

    private static void encryptDirectMessage(@NonNull IIdentity senderId, @NonNull DirectMessageEvent directMessageEvent) {

        ITag pkTag = directMessageEvent.getTags().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            var rcptPublicKey = pubKeyTag.getPublicKey();
            MessageCipher cipher = new MessageCipher04(senderId.getPrivateKey().getRawData(), rcptPublicKey.getRawData());
            var encryptedContent = cipher.encrypt(directMessageEvent.getContent());
            directMessageEvent.setContent(encryptedContent);
        }
    }

    public static String decrypt(@NonNull IIdentity rcptId, @NonNull GenericEvent event) {
        var recipient = event.getTags()
                .stream()
                .filter(t -> t.getCode().equalsIgnoreCase("p"))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No matching p-tag found."));
        var pTag = (PubKeyTag) recipient;

        boolean rcptFlag = amITheRecipient(rcptId, event);

        if (!rcptFlag) { // I am the message sender
            MessageCipher cipher = new MessageCipher04(rcptId.getPrivateKey().getRawData(), pTag.getPublicKey().getRawData());
            return cipher.decrypt(event.getContent());
        }

        // I am the message recipient
        var sender = event.getPubKey();
        log.log(Level.FINE, "The message is being decrypted for {0}", sender);
        MessageCipher cipher = new MessageCipher04(rcptId.getPrivateKey().getRawData(), sender.getRawData());
        return cipher.decrypt(event.getContent());
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
