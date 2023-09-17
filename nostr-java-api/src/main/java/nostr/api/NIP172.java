/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
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

    /**
     * 
     */
    @Getter
    public enum StatusCode {

        SUCCESS("success"),
        ERROR("error");

        private final String value;

        StatusCode(String value) {
            this.value = value;
        }

    }

    /**
     * 
     * @param pubKey
     * @param jobType
     * @param jobModel
     * @param inputTagData
     * @param inputTagType
     * @param inputTypeMarker
     * @param relays
     * @param bidTagAmount
     * @param bidTagMaxPrice
     * @param expiration
     * @param serviceProviders
     * @param uniqueJobName
     * @return 
     */
    public static GenericEvent createJobRequestEvent(PublicKey pubKey, String jobType, String jobModel, String inputTagData, String inputTagType, Marker inputTypeMarker, List<Relay> relays, Integer bidTagAmount, Integer bidTagMaxPrice, Integer expiration, List<PublicKey> serviceProviders, String uniqueJobName) {
        List<BaseTag> tags = new ArrayList<>();
        tags.add(createJobTag(jobType, jobModel));
        tags.add(createInputTag(jobType, inputTagType, inputTypeMarker));
        tags.add(new nostr.api.factory.impl.NIP42.RelaysTagFactory(relays).create());
        tags.add(createBidTag(bidTagAmount, bidTagMaxPrice));
        tags.add(new nostr.api.factory.impl.NIP40.ExpirationTagFactory(expiration).create());
        tags.add(new nostr.api.factory.impl.NIP33.IdentifierTagFactory(uniqueJobName).create());
        serviceProviders.forEach(p -> tags.add(PubKeyTag.builder().publicKey(p).build()));

        PublicKey sender = Identity.getInstance().getPublicKey();
        return new GenericEvent(sender, 68001, tags, null);
    }

    /**
     * 
     * @param pubKey
     * @param request
     * @param eventId
     * @param jobRequester
     * @param status
     * @param statusMoreInfo
     * @param requestedPaymentAmount
     * @return 
     */
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

    /**
     * 
     * @param jobType
     * @param jobModel
     * @return 
     */
    public static GenericTag createJobTag(String jobType, String jobModel) {
        List<ElementAttribute> attributes = new ArrayList<>();
        attributes.add(0, new ElementAttribute("param0", jobType, 172));
        attributes.add(1, new ElementAttribute("param1", jobModel, 172));
        return new GenericTag("j", 172, attributes);
    }

    /**
     * 
     * @param data
     * @param inputType
     * @param marker
     * @return 
     */
    public static GenericTag createInputTag(String data, String inputType, Marker marker) {
        List<ElementAttribute> attributes = new ArrayList<>();
        attributes.add(0, new ElementAttribute("param0", data, 172));
        attributes.add(1, new ElementAttribute("param1", inputType, 172));
        attributes.add(2, new ElementAttribute("param2", marker, 172));
        return new GenericTag("input", 172, attributes);
    }

    /**
     * 
     * @param satAmount
     * @param maxPrice
     * @return 
     */
    public static GenericTag createBidTag(Integer satAmount, Integer maxPrice) {
        List<ElementAttribute> attributes = new ArrayList<>();
        attributes.add(0, new ElementAttribute("param0", satAmount, 172));
        attributes.add(1, new ElementAttribute("param1", maxPrice, 172));
        return new GenericTag("bid", 172, attributes);
    }

    /**
     * 
     * @param status
     * @param statusMoreInfo
     * @return 
     */
    public static GenericTag createStatusTag(StatusCode status, String statusMoreInfo) {
        List<ElementAttribute> attributes = new ArrayList<>();
        attributes.add(0, ElementAttribute.builder().name("param0").nip(172).value(status).build());
        attributes.add(1, ElementAttribute.builder().name("param1").nip(172).value(statusMoreInfo).build());
        return new GenericTag("status", 172, attributes);
    }

    /**
     * 
     * @param value
     * @return 
     */
    public static GenericTag createRequestTag(String value) {
        return new TagFactory("request", 172, value).create();
    }
    
    /**
     * 
     * @param requestedPaymentAmount
     * @return 
     */
    public static GenericTag createPaymentTag(Integer requestedPaymentAmount) {
        return new TagFactory("payment", 172, requestedPaymentAmount.toString()).create();
    }
}
