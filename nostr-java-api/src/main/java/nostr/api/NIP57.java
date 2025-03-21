package nostr.api;

import lombok.NonNull;
import nostr.api.factory.TagFactory;
import nostr.api.factory.impl.NIP57Impl.ZapReceiptEventFactory;
import nostr.api.factory.impl.NIP57Impl.ZapRequestEventFactory;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.impl.GenericEvent;
import nostr.event.tag.GenericTag;
import nostr.event.impl.ZapRequest;
import nostr.event.tag.AddressTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.IdentifierTag;
import nostr.event.tag.PubKeyTag;
import nostr.event.tag.RelaysTag;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author eric
 */
public class NIP57<T extends GenericEvent> extends EventNostr<T> {
    private static final String LNURL_TAG_NAME = "lnurl";
    private static final String BOLT11_TAG_NAME = "bolt11";
    private static final String PREIMAGE_TAG_NAME = "preimage";
    private static final String DESCRIPTION_TAG_NAME = "description";
    private static final String AMOUNT_TAG_NAME = "amount";
    private static final String ZAP_TAG_NAME = "zap";

    public NIP57(@NonNull Identity sender) {
        setSender(sender);
    }

    public NIP57<T> createZapRequestEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> baseTags, String content, @NonNull ZapRequest zapRequest) {
        setEvent((T) new ZapRequestEventFactory(getSender(), recipientPubKey, baseTags, content, zapRequest).create());
        return this;
    }

    public NIP57<T> createZapRequestEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> baseTags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull RelaysTag relaysTags) {
        return createZapRequestEvent(recipientPubKey, baseTags, content, new ZapRequest(relaysTags, amount, lnUrl));
    }

    public NIP57<T> createZapRequestEventFromList(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> baseTags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull List<Relay> relays) {
        return createZapRequestEvent(recipientPubKey, baseTags, content, amount, lnUrl, new RelaysTag(relays));
    }

    public NIP57<T> createZapRequestEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> baseTags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull List<String> relays) {
        return createZapRequestEventFromList(recipientPubKey, baseTags, content, amount, lnUrl, relays.stream().map(Relay::new).toList());
    }

    public NIP57<T> createZapRequestEvent(@NonNull PublicKey recipientPubKey, @NonNull List<BaseTag> baseTags, String content, @NonNull Long amount, @NonNull String lnUrl, @NonNull String... relaysTags) {
        return createZapRequestEvent(recipientPubKey, baseTags, content, amount, lnUrl, List.of(relaysTags));
    }

    public NIP57<T> createZapReceiptEvent(@NonNull PubKeyTag zapRequestPubKeyTag, List<BaseTag> baseTags, EventTag zapRequestEventTag, AddressTag zapRequestAddressTag, @NonNull String bolt11,
        @NonNull String descriptionSha256, @NonNull String preimage) {
        setEvent((T) new ZapReceiptEventFactory(getSender(), baseTags, zapRequestPubKeyTag, zapRequestEventTag, zapRequestAddressTag, bolt11, descriptionSha256, preimage).create());
        return this;
    }

    public NIP57<T> createZapReceiptEvent(@NonNull String zapRequestPubKeyTag, List<BaseTag> baseTags, String zapRequestEventTag, String zapReceiptAddressTag, String zapReceiptIdentifier, String zapReceiptRelayUri, String bolt11, String descriptionSha256, String preimage) {
        return createZapReceiptEvent(new PubKeyTag(new PublicKey(zapRequestPubKeyTag)), baseTags, new EventTag(zapRequestEventTag), new AddressTag(null, new PublicKey(zapReceiptAddressTag), new IdentifierTag(zapReceiptIdentifier), new Relay(zapReceiptRelayUri)), bolt11, descriptionSha256, preimage);
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

    public NIP57<T> addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
        getEvent().addTag(createZapTag(receiver, relays, weight));
        return this;
    }

    public NIP57<T> addZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
        getEvent().addTag(createZapTag(receiver, relays));
        return this;
    }

    public NIP57<T> addRelaysTag(@NonNull RelaysTag relaysTag) {
        getEvent().addTag(relaysTag);
        return this;
    }

    public NIP57<T> addRelaysList(@NonNull List<Relay> relays) {
        return addRelaysTag(new RelaysTag(relays));
    }

    public NIP57<T> addRelays(@NonNull List<String> relays) {
        return addRelaysList(relays.stream().map(Relay::new).toList());
    }

    public NIP57<T> addRelays(@NonNull String... relays) {
        return addRelays(List.of(relays));
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
     * @param relays
     * @param weight
     */
    public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays, Integer weight) {
        List<ElementAttribute> attributes = new ArrayList<>();
        var receiverAttr = new ElementAttribute("receiver", receiver.toString());
        var relayAttrs = relays.stream().map(relay -> new ElementAttribute("relay", relay.getUri())).toList();
        if (weight != null) {
            var weightAttr = new ElementAttribute("weight", weight);
            attributes.add(weightAttr);
        }

        attributes.add(receiverAttr);
        attributes.addAll(relayAttrs);
        return new GenericTag(ZAP_TAG_NAME, attributes);
    }

    /**
     * @param receiver
     * @param relays
     */
    public static GenericTag createZapTag(@NonNull PublicKey receiver, @NonNull List<Relay> relays) {
        return createZapTag(receiver, relays, null);
    }
}
