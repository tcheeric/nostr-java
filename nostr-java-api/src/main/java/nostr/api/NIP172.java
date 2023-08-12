/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nostr.api.factory.TagFactory;
import nostr.base.ElementAttribute;
import nostr.base.PublicKey;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.Marker;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.GenericTag;
import nostr.event.tag.EventTag;
import nostr.event.tag.PubKeyTag;
import nostr.id.Identity;

/**
 *
 * @author eric
 */
public class NIP172 {

    public enum StatusCode {

        SUCCESS("success"),
        ERROR("error");

        private final String value;

        private StatusCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static GenericEvent createJobRequestEvent(PublicKey pubKey, String jobType, String jobModel, String inputTagData, String inputTagType, Marker inputTypeMarker, List<Relay> relays, Integer bidTagAmount, Integer bidTagMaxPrice, Integer expiration, List<PublicKey> serviceProviders, String uniqueJobName) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(createJobTag(jobType, jobModel));
        tags.add(createInputTag(jobType, inputTagType, inputTypeMarker));
        tags.add(new nostr.api.factory.impl.NIP42.RelaysTagFactory(relays).create());
        tags.add(createBidTag(bidTagAmount, bidTagMaxPrice));
        tags.add(new nostr.api.factory.impl.NIP40.ExpirationTagFactory(expiration).create());
        tags.add(new nostr.api.factory.impl.NIP33.IdentifierTagFactory(uniqueJobName).create());
        serviceProviders.stream().forEach(p -> tags.add(PubKeyTag.builder().publicKey(p).build()));

        PublicKey sender = Identity.getInstance().getPublicKey();
        return new GenericEvent(sender, 68001, tags, null);
    }

    public static GenericEvent createJobResultEvent(PublicKey pubKey, String request, String eventId, PublicKey jobRequester, StatusCode status, String statusMoreInfo, Integer requestedPaymentAmount) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(EventTag.builder().idEvent(eventId).build());
        tags.add(PubKeyTag.builder().publicKey(pubKey).build());
        tags.add(createRequestTag(request));
        tags.add(createStatusTag(status, statusMoreInfo));
        tags.add(createPaymentTag(requestedPaymentAmount));

        PublicKey sender = Identity.getInstance().getPublicKey();
        return new GenericEvent(sender, 68002, tags, null);
    }

    public static GenericTag createJobTag(String jobType, String jobModel) {
        Set<ElementAttribute> attributes = new HashSet<>();
        attributes.add(new ElementAttribute("param0", jobType, 172));
        attributes.add(new ElementAttribute("param1", jobModel, 172));
        return new GenericTag("j", 172, attributes);
    }

    public static GenericTag createInputTag(String data, String inputType, Marker marker) {
        Set<ElementAttribute> attributes = new HashSet<>();
        attributes.add(new ElementAttribute("param0", data, 172));
        attributes.add(new ElementAttribute("param1", inputType, 172));
        attributes.add(new ElementAttribute("param2", marker, 172));
        return new GenericTag("input", 172, attributes);
    }

    public static GenericTag createBidTag(Integer satAmount, Integer maxPrice) {
        Set<ElementAttribute> attributes = new HashSet<>();
        attributes.add(new ElementAttribute("param0", satAmount, 172));
        attributes.add(new ElementAttribute("param1", maxPrice, 172));
        return new GenericTag("bid", 172, attributes);
    }

    public static GenericTag createStatusTag(StatusCode status, String statusMoreInfo) {
        Set<ElementAttribute> attributes = new HashSet<>();
        attributes.add(ElementAttribute.builder().name("param0").nip(172).value(status).build());
        attributes.add(ElementAttribute.builder().name("param1").nip(172).value(statusMoreInfo).build());
        return new GenericTag("status", 172, attributes);
    }

    public static GenericTag createRequestTag(String value) {
        return new TagFactory("request", 172, value).create();
    }
    
    public static GenericTag createPaymentTag(Integer requestedPaymentAmount) {
        return new TagFactory("payment", 172, requestedPaymentAmount.toString()).create();
    }
}
