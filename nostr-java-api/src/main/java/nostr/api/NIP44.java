package nostr.api;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.nip44.MessageCipher44;
import nostr.event.BaseTag;
import nostr.event.impl.EncryptedPayloadEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.IIdentity;
import nostr.util.NostrException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

@Log
public class NIP44 extends Nostr {

    /**
     * Create a NIP44 Encrypted Payload
     *
     * @param recipient the EP recipient
     * @param content   the EP content in clear-text
     * @return the EP event
     */
    public static EncryptedPayloadEvent createDirectMessageEvent(@NonNull IIdentity sender, @NonNull PublicKey recipient, @NonNull String content) {
        return new nostr.api.factory.impl.NIP44.EncryptedPayloadEventFactory(sender, recipient, content).create();
    }

    /**
     * Create a NIP44 Encrypted Payload
     *
     * @param recipient the EP recipient
     * @param content   the EP content in clear-text
     * @return the EP event
     */
    public static EncryptedPayloadEvent createDirectMessageEvent(@NonNull PublicKey recipient, @NonNull String content) {
        return new nostr.api.factory.impl.NIP44.EncryptedPayloadEventFactory(recipient, content).create();
    }

    /**
     * Create a NIP44 Encrypted Payload
     *
     * @param tags      additional note's tags
     * @param recipient the EP recipient
     * @param content   the EP content
     * @return the EP event
     */
    public static EncryptedPayloadEvent createDirectMessageEvent(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
        return new nostr.api.factory.impl.NIP44.EncryptedPayloadEventFactory(tags, recipient, content).create();
    }

    /**
     * Encrypt a Encrypted Payload event
     *
     * @param senderId
     * @param ep       the EP event
     */
    public static void encrypt(@NonNull IIdentity senderId, @NonNull EncryptedPayloadEvent ep) {
        encryptDirectMessage(senderId, ep);
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


    public static String encrypt(@NonNull IIdentity senderId, @NonNull String message, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher44(senderId.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.encrypt(message);
        //return new IdentityHelper(senderId).encrypt(message, recipient);
    }

    /**
     * Decrypt an encrypted Payloads
     *
     * @param rcptId
     * @param ep     the encrypted Payloads
     * @return the ep content in clear-text
     * @throws NostrException
     */
    public static String decrypt(@NonNull IIdentity rcptId, @NonNull EncryptedPayloadEvent ep) throws NostrException {
        return NIP44.decrypt(rcptId, (GenericEvent) ep);
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
        log.log(Level.INFO, "The message is being decrypted for {0}", sender);
        MessageCipher cipher = new MessageCipher44(rcptId.getPrivateKey().getRawData(), sender.getRawData());
        return cipher.decrypt(event.getContent());
    }

    public static String decrypt(@NonNull IIdentity identity, @NonNull String encrypteEPessage, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher44(identity.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.decrypt(encrypteEPessage);
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
