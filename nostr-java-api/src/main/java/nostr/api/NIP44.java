package nostr.api;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP44Impl;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.nip44.MessageCipher44;
import nostr.event.BaseTag;
import nostr.event.impl.EncryptedPayloadEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.id.Identity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

@Log
public class NIP44<T extends GenericEvent> extends EventNostr<T> {

    public NIP44(@NonNull IIdentity sender, @NonNull PublicKey recipient) {
        setSender(sender);
        setRecipient(recipient);
    }

    /**
     * Create a NIP44 Encrypted Payload
     *
     * @param content   the EP content in clear-text
     * @return the EP event
     */
    public NIP44<T> createDirectMessageEvent(@NonNull String content) {
        var encryptedContent = encrypt(getSender(), content, getRecipient());
        var factory = new NIP44Impl.EncryptedPayloadEventFactory(getSender(), getRecipient(), encryptedContent);
        var event = factory.create();
        setEvent((T) event);
        return this;
    }

    /**
     * Create a NIP44 Encrypted Payload
     *
     * @param tags      additional note's tags
     * @param recipient the EP recipient
     * @param content   the EP content
     * @return the EP event
     */
    public NIP44<T> createDirectMessageEvent(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
        var encryptedContent = encrypt(getSender(), content, recipient);
        var factory = new NIP44Impl.EncryptedPayloadEventFactory(getSender(), tags, recipient, encryptedContent);
        var event = factory.create();
        setEvent((T) event);
        return this;
    }

    /**
     * Encrypt a Encrypted Payload event
     *
     * @param senderId
     * @param ep       the EP event
     */
    public static void encrypt(@NonNull Identity senderId, @NonNull EncryptedPayloadEvent ep) {
        encryptDirectMessage(senderId, ep);
    }

    public NIP44<T> encrypt() {
        encryptDirectMessage(getSender(), (EncryptedPayloadEvent) getEvent());
        return this;
    }

    public static String encrypt(@NonNull IIdentity senderId, @NonNull String message, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher44(senderId.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.encrypt(message);
    }

    /**
     * Decrypt an encrypted Payloads
     *
     * @param rcptId
     * @param ep     the encrypted Payloads
     * @return the ep content in clear-text
     */
    public static String decrypt(@NonNull IIdentity rcptId, @NonNull EncryptedPayloadEvent ep) {
        return NIP44.decrypt(rcptId, (GenericEvent) ep);
    }

    /**
     * Decrypt an encrypted Payloads
     *
     * @param identity  the sender's identity
     * @param encrypteEPessage  the encrypted message
     * @param recipient the recipient's public key
     * @return the ep content in clear-text
     */
    public static String decrypt(@NonNull IIdentity identity, @NonNull String encrypteEPessage, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher44(identity.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.decrypt(encrypteEPessage);
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
            MessageCipher cipher = new MessageCipher44(rcptId.getPrivateKey().getRawData(), pTag.getPublicKey().getRawData());
            return cipher.decrypt(event.getContent());
        }

        // I am the message recipient
        var sender = event.getPubKey();
        log.log(Level.FINE, "The message is being decrypted for {0}", sender);
        MessageCipher cipher = new MessageCipher44(rcptId.getPrivateKey().getRawData(), sender.getRawData());
        return cipher.decrypt(event.getContent());
    }

    private static void encryptDirectMessage(@NonNull IIdentity senderId, @NonNull EncryptedPayloadEvent ep) {

        ITag pkTag = ep.getTags().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            var rcptPublicKey = pubKeyTag.getPublicKey();
            MessageCipher cipher = new MessageCipher44(senderId.getPrivateKey().getRawData(), rcptPublicKey.getRawData());
            var encryptedContent = cipher.encrypt(ep.getContent());
            ep.setContent(encryptedContent);
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
