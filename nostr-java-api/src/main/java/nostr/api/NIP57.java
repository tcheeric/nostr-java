package nostr.api;

import lombok.NonNull;
import lombok.SneakyThrows;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.api.factory.impl.BaseTagFactory;
import nostr.base.IEvent;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.config.Constants;
import nostr.event.BaseTag;
import nostr.event.entities.ZapRequest;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.EventTag;
import nostr.event.tag.GenericTag;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP57 extends EventNostr {

    public NIP57(@NonNull Identity sender) {
        setSender(sender);
    }

    public NIP57 createZapRequestEvent(
            @NonNull ZapRequest zapRequest,
            @NonNull String content,
            PublicKey recipientPubKey,
            GenericEvent zappedEvent,
            BaseTag addressTag) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.ZAP_REQUEST, content).create();

        genericEvent.addTag(zapRequest.getRelaysTag());
        genericEvent.addTag(createAmountTag(zapRequest.getAmount()));
        genericEvent.addTag(createLnurlTag(zapRequest.getLnUrl()));

        if (recipientPubKey != null) {
            genericEvent.addTag(NIP01.createPubKeyTag(recipientPubKey));
        }

        if (zappedEvent != null) {
            genericEvent.addTag(NIP01.createEventTag(zappedEvent.getId()));
        }

        if (addressTag != null) {
            if (!Constants.Tag.ADDRESS_CODE.equals(addressTag.getCode())) { // Sanity check
                throw new IllegalArgumentException("tag must be of type AddressTag");
            }
            genericEvent.addTag(addressTag);
        }

        this.updateEvent(genericEvent);
        return this;
    }

    public NIP57 createZapRequestEvent(
            @NonNull Long amount,
            @NonNull String lnUrl,
            @NonNull BaseTag relaysTags,
            @NonNull String content,
            PublicKey recipientPubKey,
            GenericEvent zappedEvent,
            BaseTag addressTag) {

        if (!relaysTags.getCode().equals(Constants.Tag.RELAYS_CODE)) {
            throw new IllegalArgumentException("tag must be of type RelaysTag");
        }

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.ZAP_REQUEST, content).create();

        genericEvent.addTag(relaysTags);
        genericEvent.addTag(createAmountTag(amount));
        genericEvent.addTag(createLnurlTag(lnUrl));

        if (recipientPubKey != null) {
            genericEvent.addTag(NIP01.createPubKeyTag(recipientPubKey));
        }

        if (zappedEvent != null) {
            genericEvent.addTag(NIP01.createEventTag(zappedEvent.getId()));
        }

        if (addressTag != null) {
            if (!addressTag.getCode().equals(Constants.Tag.ADDRESS_CODE)) { // Sanity check
                throw new IllegalArgumentException("Address tag must be of type AddressTag");
            }
            genericEvent.addTag(addressTag);
        }

        this.updateEvent(genericEvent);
        return this;
    }

    public NIP57 createZapRequestEvent(
            @NonNull Long amount,
            @NonNull String lnUrl,
            @NonNull List<Relay> relays,
            @NonNull String content,
            PublicKey recipientPubKey,
            GenericEvent zappedEvent,
            BaseTag addressTag) {

        return createZapRequestEvent(amount, lnUrl, new RelaysTag(relays), content, recipientPubKey, zappedEvent, addressTag);
    }

    public NIP57 createZapRequestEvent(
            @NonNull Long amount,
            @NonNull String lnUrl,
            @NonNull List<String> relays,
            @NonNull String content,
            PublicKey recipientPubKey) {

        return createZapRequestEvent(amount, lnUrl, relays.stream().map(Relay::new).toList(), content, recipientPubKey, null, null);
    }

    @SneakyThrows
    public NIP57 createZapReceiptEvent(
            @NonNull GenericEvent zapRequestEvent,
            @NonNull String bolt11,
            @NonNull String preimage,
            @NonNull PublicKey zapRecipient) {

        GenericEvent genericEvent = new GenericEventFactory(getSender(), Constants.Kind.ZAP_RECEIPT, "").create();

        // Add the tags
        genericEvent.addTag(NIP01.createPubKeyTag(zapRecipient));

        // Zap receipt tags
        String descriptionSha256 = IEvent.MAPPER_AFTERBURNER.writeValueAsString(zapRequestEvent);
        genericEvent.addTag(createDescriptionTag(StringEscapeUtils.escapeJson(descriptionSha256)));
        genericEvent.addTag(createBolt11Tag(bolt11));
        genericEvent.addTag(createPreImageTag(preimage));
        genericEvent.addTag(createZapSenderPubKeyTag(zapRequestEvent.getPubKey()));
        genericEvent.addTag(NIP01.createEventTag(zapRequestEvent.getId()));

        GenericTag addressTag = (GenericTag) zapRequestEvent.getTags().stream()
                .filter(tag -> tag.getCode().equals(Constants.Tag.ADDRESS_CODE))
                .findFirst()
                .orElse(null);

        if (addressTag != null) {
            genericEvent.addTag(addressTag);
        }

        genericEvent.setCreatedAt(zapRequestEvent.getCreatedAt());

        // Set the event
        this.updateEvent(genericEvent);

        // Return this
        return this;
    }

    public NIP57 addLnurlTag(@NonNull String lnurl) {
        getEvent().addTag(createLnurlTag(lnurl));
        return this;
    }

    public NIP57 addEventTag(@NonNull EventTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    public NIP57 addBolt11Tag(@NonNull String bolt11) {
        getEvent().addTag(createBolt11Tag(bolt11));
        return this;
    }

    public NIP57 addPreImageTag(@NonNull String preimage) {
        getEvent().addTag(createPreImageTag(preimage));
        return this;
    }

    public NIP57 addDescriptionTag(@NonNull String description) {
        getEvent().addTag(createDescriptionTag(description));
        return this;
    }

    public NIP57 addAmountTag(@NonNull Integer amount) {
        getEvent().addTag(createAmountTag(amount));
        return this;
    }

    public NIP57 addRecipientTag(@NonNull PublicKey recipient) {
        getEvent().addTag(NIP01.createPubKeyTag(recipient));
        return this;
    }

    public NIP57 addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
        getEvent().addTag(createZapTag(receiver, relays, weight));
        return this;
    }

    public NIP57 addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
        getEvent().addTag(createZapTag(receiver, relays));
        return this;
    }

    public NIP57 addRelaysTag(@NonNull RelaysTag relaysTag) {
        getEvent().addTag(relaysTag);
        return this;
    }

    public NIP57 addRelaysList(@NonNull List<Relay> relays) {
        return addRelaysTag(new RelaysTag(relays));
    }

    public NIP57 addRelays(@NonNull List<String> relays) {
        return addRelaysList(relays.stream().map(Relay::new).toList());
    }

    public NIP57 addRelays(@NonNull String... relays) {
        return addRelays(List.of(relays));
    }

    /**
     * @param lnurl
     * @return
     */
    public static BaseTag createLnurlTag(@NonNull String lnurl) {
        return new BaseTagFactory(Constants.Tag.LNURL_CODE, lnurl).create();
    }

    /**
     * @param bolt11
     * @return
     */
    public static BaseTag createBolt11Tag(@NonNull String bolt11) {
        return new BaseTagFactory(Constants.Tag.BOLT11_CODE, bolt11).create();
    }

    /**
     * @param preimage
     */
    public static BaseTag createPreImageTag(@NonNull String preimage) {
        return new BaseTagFactory(Constants.Tag.PREIMAGE_CODE, preimage).create();
    }

    /**
     * @param description
     */
    public static BaseTag createDescriptionTag(@NonNull String description) {
        return new BaseTagFactory(Constants.Tag.DESCRIPTION_CODE, description).create();
    }

    /**
     * @param amount
     */
    public static BaseTag createAmountTag(@NonNull Number amount) {
        return new BaseTagFactory(Constants.Tag.AMOUNT_CODE, amount.toString()).create();
    }

    /**
     * @param publicKey
     */
    public static BaseTag createZapSenderPubKeyTag(@NonNull PublicKey publicKey) {
        return new BaseTagFactory(Constants.Tag.RECIPIENT_PUBKEY_CODE, publicKey.toString()).create();
    }

    /**
     * @param receiver
     * @param relays
     * @param weight
     */
    public static BaseTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
        List<String> params = new ArrayList<>();
        params.add(receiver.toString());
        relays.stream()
                .map(Relay::getUri)
                .forEach(params::add);
        if (weight != null) {
            params.add(weight.toString());
        }
        return BaseTag.create(Constants.Tag.ZAP_CODE, params);
    }

    /**
     * @param receiver
     * @param relays
     */
    public static BaseTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
        return createZapTag(receiver, relays, null);
    }
}
