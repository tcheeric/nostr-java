/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.api.factory.impl.NIP42.RelaysTagFactory;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP57 extends Nostr {

    /**
     *
     * @param amount
     * @param lnurl
     * @param relay
     * @param recipient
     * @param eventTag
     * @param content
     * @return
     */
    public static GenericEvent createZapEvent(@NonNull Integer amount, @NonNull String lnurl, Relay relay, @NonNull PublicKey recipient, EventTag eventTag, String content) {
        var tags = new ArrayList<BaseTag>();
        tags.add(createLnurlTag(lnurl));
        tags.add(createAmountTag(amount));
        tags.add(new RelaysTagFactory(relay).create());
        tags.add(PubKeyTag.builder().publicKey(recipient).build());
        tags.add(eventTag);

        var sender = Identity.getInstance().getPublicKey();
        return new GenericEvent(sender, 9734, tags, content);
    }

    /**
     *
     * @param zapEvent
     * @param bolt11
     * @param description
     * @param preimage
     * @param recipient
     * @param eventTag
     * @return
     */
    public static GenericEvent createZapReceiptEvent(@NonNull GenericEvent zapEvent, @NonNull String bolt11, String description, @NonNull String preimage, @NonNull PublicKey recipient, @NonNull EventTag eventTag) {
        var tags = new ArrayList<BaseTag>();
        tags.add(createBolt11Tag(bolt11));
        tags.add(createDescriptionTag(description));
        tags.add(createPreImageTag(preimage));
        tags.add(new PubKeyTag(recipient, null, null));
        tags.add(eventTag);

        var sender = Identity.getInstance().getPublicKey();
        return new GenericEvent(sender, 9735, tags, Nostr.Json.encode(zapEvent));
    }

    /**
     *
     * @param lnurl
     * @return
     */
    public static GenericTag createLnurlTag(@NonNull String lnurl) {
        return new TagFactory("lnurl", 57, lnurl).create();
    }

    /**
     *
     * @param bolt11
     * @return
     */
    public static GenericTag createBolt11Tag(@NonNull String bolt11) {
        return new TagFactory("lnurl", 57, bolt11).create();
    }

    /**
     *
     * @param preimage
     * @return
     */
    public static GenericTag createPreImageTag(@NonNull String preimage) {
        return new TagFactory("lnurl", 57, preimage).create();
    }

    /**
     *
     * @param description
     * @return
     */
    public static GenericTag createDescriptionTag(@NonNull String description) {
        return new TagFactory("lnurl", 57, description).create();
    }

    /**
     *
     * @param amount
     * @return
     */
    public static GenericTag createAmountTag(@NonNull Integer amount) {
        return new TagFactory("lnurl", 57, amount.toString()).create();
    }

    /**
     *
     * @param receiver
     * @param relay
     * @param weight
     * @return
     */
    public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull Relay relay, Integer weight) {
        List<ElementAttribute> attributes = new ArrayList<>();
        var receiverAttr = new ElementAttribute("receiver", receiver.toString(), 57);
        var relayAttr = new ElementAttribute("relay", relay.getHostname(), 57);
        if (weight != null) {
            var weightAttr = new ElementAttribute("weight", weight, 57);
            attributes.add(weightAttr);
        }

        attributes.add(receiverAttr);
        attributes.add(relayAttr);

        return new GenericTag("zap", 57, attributes);
    }

    /**
     *
     * @param receiver
     * @param relay
     * @return
     */
    public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull Relay relay) {
        return createZapTag(receiver, relay, null);
    }
}
