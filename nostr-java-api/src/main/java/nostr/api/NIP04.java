/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP04.DirectMessageEventFactory;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.nip04.MessageCipher04;
import nostr.event.BaseTag;
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
public class NIP04 extends Nostr {

    /**
     * Create a NIP04 Encrypted Direct Message
     *
     * @param recipient the DM recipient
     * @param content   the DM content in clear-text
     * @return the DM event
     */
    public static DirectMessageEvent createDirectMessageEvent(@NonNull IIdentity sender, @NonNull PublicKey recipient, @NonNull String content) {
        return new DirectMessageEventFactory(sender, recipient, content).create();
    }

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
     * @param senderId
     * @param dm       the DM event
     */
    public static void encrypt(@NonNull IIdentity senderId, @NonNull DirectMessageEvent dm) {
        encryptDirectMessage(senderId, dm);
    }

    private static void encryptDirectMessage(@NonNull IIdentity senderId, @NonNull DirectMessageEvent dmEvent) {

        ITag pkTag = dmEvent.getTags().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            var rcptPublicKey = pubKeyTag.getPublicKey();
            MessageCipher cipher = new MessageCipher04(senderId.getPrivateKey().getRawData(), rcptPublicKey.getRawData());
            var encryptedContent = cipher.encrypt(dmEvent.getContent());
            dmEvent.setContent(encryptedContent);
        }
    }


    public static String encrypt(@NonNull IIdentity senderId, @NonNull String message, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher04(senderId.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.encrypt(message);
        //return new IdentityHelper(senderId).encrypt(message, recipient);
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

    public static String decrypt(@NonNull IIdentity rcptId, @NonNull GenericEvent event) throws NostrException {
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
            //return new IdentityHelper(rcptId).decryptMessage(event.getContent(), pTag.getPublicKey());
        }

        // I am the message recipient
        var sender = event.getPubKey();
        log.log(Level.INFO, "The message is being decrypted for {0}", sender);
        MessageCipher cipher = new MessageCipher04(rcptId.getPrivateKey().getRawData(), sender.getRawData());
        return cipher.decrypt(event.getContent());
        //return new IdentityHelper(rcptId).decryptMessage(event.getContent(), sender);
    }

    public static String decrypt(@NonNull IIdentity identity, @NonNull String encryptedMessage, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher04(identity.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.decrypt(encryptedMessage);
        //return new IdentityHelper(identity).decryptMessage(encryptedMessage, recipient);
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
