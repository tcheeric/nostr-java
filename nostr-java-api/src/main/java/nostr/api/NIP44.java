package nostr.api;

import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP44Impl;
import nostr.base.ITag;
import nostr.base.PublicKey;
import nostr.encryption.MessageCipher;
import nostr.encryption.MessageCipher44;
import nostr.event.BaseTag;
import nostr.event.filter.Filterable;
import nostr.event.impl.EncryptedPayloadEvent;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;

@Log
public class NIP44<T extends GenericEvent> extends EventNostr<T> {

    public NIP44(@NonNull Identity sender, @NonNull PublicKey recipient) {
        setSender(sender);
        setRecipient(recipient);
    }

    public NIP44<T> createDirectMessageEvent(@NonNull String content) {
        var encryptedContent = encrypt(getSender(), content, getRecipient());
        var factory = new NIP44Impl.EncryptedPayloadEventFactory(getSender(), getRecipient(), encryptedContent);
        var event = factory.create();
        setEvent((T) event);
        return this;
    }

    public NIP44<T> createDirectMessageEvent(@NonNull List<BaseTag> tags, @NonNull PublicKey recipient, @NonNull String content) {
        var encryptedContent = encrypt(getSender(), content, recipient);
        var factory = new NIP44Impl.EncryptedPayloadEventFactory(getSender(), tags, recipient, encryptedContent);
        var event = factory.create();
        setEvent((T) event);
        return this;
    }

    public static void encrypt(@NonNull Identity sender, @NonNull EncryptedPayloadEvent ep) {
        encryptDirectMessage(sender, ep);
    }

    public NIP44<T> encrypt() {
        encryptDirectMessage(getSender(), (EncryptedPayloadEvent) getEvent());
        return this;
    }

    public static String encrypt(@NonNull Identity sender, @NonNull String message, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher44(sender.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.encrypt(message);
    }

    public static String decrypt(@NonNull Identity recipient, @NonNull EncryptedPayloadEvent ep) {
        return NIP44.decrypt(recipient, (GenericEvent) ep);
    }

    public static String decrypt(@NonNull Identity identity, @NonNull String encrypteEPessage, @NonNull PublicKey recipient) {
        MessageCipher cipher = new MessageCipher44(identity.getPrivateKey().getRawData(), recipient.getRawData());
        return cipher.decrypt(encrypteEPessage);
    }

    public static String decrypt(@NonNull Identity recipient, @NonNull GenericEvent event) {
        boolean rcptFlag = amITheRecipient(recipient, event);

        if (!rcptFlag) { // I am the message sender
            MessageCipher cipher = new MessageCipher44(recipient.getPrivateKey().getRawData(),
                    Filterable.getTypeSpecificTags(PubKeyTag.class, event)
                            .stream()
                            .findFirst()
                            .orElseThrow(() -> new NoSuchElementException("No matching p-tag found.")).getPublicKey().getRawData());
            return cipher.decrypt(event.getContent());
        }

        // I am the message recipient
        var sender = event.getPubKey();
        log.log(Level.FINE, "The message is being decrypted for {0}", sender);
        MessageCipher cipher = new MessageCipher44(recipient.getPrivateKey().getRawData(), sender.getRawData());
        return cipher.decrypt(event.getContent());
    }

    private static void encryptDirectMessage(@NonNull Identity sender, @NonNull EncryptedPayloadEvent ep) {

        ITag pkTag = ep.getTags().get(0);
        if (pkTag instanceof PubKeyTag pubKeyTag) {
            var rcptPublicKey = pubKeyTag.getPublicKey();
            MessageCipher cipher = new MessageCipher44(sender.getPrivateKey().getRawData(), rcptPublicKey.getRawData());
            var encryptedContent = cipher.encrypt(ep.getContent());
            ep.setContent(encryptedContent);
        }
    }

    private static boolean amITheRecipient(@NonNull Identity recipient, @NonNull GenericEvent event) {
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
