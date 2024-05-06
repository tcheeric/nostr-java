/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.EventTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP57<T extends GenericEvent> extends EventNostr<T> {


    private static final int ZAP_EVENT_KIND = 9734;
    private static final int ZAP_RECEIPT_EVENT_KIND = 9735;
    private static final String LNURL_TAG_NAME = "lnurl";
    private static final String BOLT11_TAG_NAME = "bolt11";
    private static final String PREIMAGE_TAG_NAME = "preimage";
    private static final String DESCRIPTION_TAG_NAME = "description";
    private static final String AMOUNT_TAG_NAME = "amount";
    private static final String ZAP_TAG_NAME = "zap";

    public NIP57(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * @param content
     */
    public NIP57<T> createZapEvent(String content) {
        var factory = new GenericEventFactory(getSender(), ZAP_EVENT_KIND, content);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * @param amount
     * @param lnurl
     * @param relay
     * @param recipient
     * @param eventTag
     * @param content
     */
    public NIP57<T> createZapEvent(@NonNull Integer amount, @NonNull String lnurl, Relay relay, @NonNull PublicKey recipient, EventTag eventTag, String content) {
        var factory = new GenericEventFactory(getSender(), ZAP_EVENT_KIND, content);
        var event = factory.create();
        setEvent((T) event);

        return this.addAmountTag(amount)
                .addLnurlTag(lnurl)
                .addRelayTag(relay)
                .addRecipientTag(recipient)
                .addEventTag(eventTag);
    }

    /**
     *
     */
    public NIP57<T> createZapReceiptEvent() {
        var factory = new GenericEventFactory(getSender(), ZAP_RECEIPT_EVENT_KIND, "");
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * @param zapEvent
     * @param bolt11
     * @param preimage
     * @param recipient
     * @param eventTag
     * @return
     */
    public NIP57<T> createZapReceiptEvent(@NonNull GenericEvent zapEvent, @NonNull String bolt11, @NonNull String preimage, @NonNull PublicKey recipient, @NonNull EventTag eventTag) {
        var factory = new GenericEventFactory(getSender(), ZAP_RECEIPT_EVENT_KIND, "");
        var event = factory.create();
        setEvent((T) event);

        return this.addBolt11Tag(bolt11)
                .addDescriptionTag(Nostr.Json.encode(zapEvent))
                .addPreImageTag(preimage)
                .addRecipientTag(recipient)
                .addEventTag(eventTag);
    }

    public NIP57<T> addLnurlTag(@NonNull String lnurl) {
        getEvent().addTag(createLnurlTag(lnurl));
        return this;
    }

    public NIP57<T> addEventTag(@NonNull EventTag tag) {
        getEvent().addTag(tag);
        return this;
    }

    public NIP57<T> addBolt11Tag(@NonNull String bolt11) {
        getEvent().addTag(createBolt11Tag(bolt11));
        return this;
    }

    public NIP57<T> addPreImageTag(@NonNull String preimage) {
        getEvent().addTag(createPreImageTag(preimage));
        return this;
    }

    public NIP57<T> addDescriptionTag(@NonNull String description) {
        getEvent().addTag(createDescriptionTag(description));
        return this;
    }

    public NIP57<T> addAmountTag(@NonNull Integer amount) {
        getEvent().addTag(createAmountTag(amount));
        return this;
    }

    public NIP57<T> addRecipientTag(@NonNull PublicKey recipient) {
        getEvent().addTag(NIP01.createPubKeyTag(recipient));
        return this;
    }

    public NIP57<T> addZapTag(@NonNull PublicKey receiver, @NonNull Relay relay, Integer weight) {
        getEvent().addTag(createZapTag(receiver, relay, weight));
        return this;
    }

    public NIP57<T> addZapTag(@NonNull PublicKey receiver, @NonNull Relay relay) {
        getEvent().addTag(createZapTag(receiver, relay));
        return this;
    }

    public NIP57<T> addRelayTag(@NonNull Relay relay) {
        getEvent().addTag(NIP42.createRelayTag(relay));
        return this;
    }

    /**
     * @param lnurl
     * @return
     */
    public static GenericTag createLnurlTag(@NonNull String lnurl) {
        return new TagFactory(LNURL_TAG_NAME, 57, lnurl).create();
    }

    /**
     * @param bolt11
     * @return
     */
    public static GenericTag createBolt11Tag(@NonNull String bolt11) {
        return new TagFactory(BOLT11_TAG_NAME, 57, bolt11).create();
    }

    /**
     * @param preimage
     */
    public static GenericTag createPreImageTag(@NonNull String preimage) {
        return new TagFactory(PREIMAGE_TAG_NAME, 57, preimage).create();
    }

    /**
     * @param description
     */
    public static GenericTag createDescriptionTag(@NonNull String description) {
        return new TagFactory(DESCRIPTION_TAG_NAME, 57, description).create();
    }

    /**
     * @param amount
     */
    public static GenericTag createAmountTag(@NonNull Integer amount) {
        return new TagFactory(AMOUNT_TAG_NAME, 57, amount.toString()).create();
    }

    /**
     * @param receiver
     * @param relay
     * @param weight
     */
    public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull Relay relay, Integer weight) {
        List<ElementAttribute> attributes = new ArrayList<>();
        var receiverAttr = new ElementAttribute("receiver", receiver.toString(), 57);
        var relayAttr = new ElementAttribute("relay", relay.getUri(), 57);
        if (weight != null) {
            var weightAttr = new ElementAttribute("weight", weight, 57);
            attributes.add(weightAttr);
        }

        attributes.add(receiverAttr);
        attributes.add(relayAttr);

        return new GenericTag(ZAP_TAG_NAME, 57, attributes);
    }

    /**
     * @param receiver
     * @param relay
     */
    public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull Relay relay) {
        return createZapTag(receiver, relay, null);
    }
}
