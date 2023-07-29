/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.base.GenericTagQuery;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.base.Relay;
import nostr.base.Signature;
import nostr.client.Client;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.Filters;
import nostr.event.json.codec.BaseEventDecoder;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.json.codec.GenericTagQueryEncoder;
import nostr.id.Identity;
import nostr.util.NostrException;

/**
 *
 * @author eric
 */
public abstract class Api {

    public static void send(@NonNull IEvent event) {
        var client = createClient();
        client.send(event);
    }

    public static Signature sign(@NonNull ISignable signable) throws NostrException {
        var identity = Identity.getInstance();
        return identity.sign(signable);
    }

    // Events
    public static String toJson(@NonNull BaseEvent event) throws NostrException {
        return Api.toJson(event, null);
    }

    public static String toJson(@NonNull BaseEvent event, Relay relay) throws NostrException {
        final var enc = new BaseEventEncoder(event, relay);
        return enc.encode();
    }

    public static BaseEvent fromJsonEvent(@NonNull String json) throws NostrException {
        final var dec = new BaseEventDecoder(json);
        return dec.decode();
    }

    // Messages
    public static String toJson(@NonNull BaseMessage message, Relay relay) throws NostrException {
        final var enc = new BaseMessageEncoder(message, relay);
        return enc.encode();
    }

    public static String toJson(@NonNull BaseMessage message) throws NostrException {
        return Api.toJson(message, null);
    }

    public static BaseMessage fromJsonMessage(@NonNull String json) throws NostrException {
        final var dec = new BaseMessageDecoder(json);
        return dec.decode();
    }

    // Tags
    public static String toJson(@NonNull BaseTag tag, Relay relay) throws NostrException {
        final var enc = new BaseTagEncoder(tag, relay);
        return enc.encode();
    }

    public static String toJson(@NonNull BaseTag tag) throws NostrException {
        return Api.toJson(tag, null);
    }

    public static BaseTag fromJsonTag(@NonNull String json) throws NostrException {
        final var dec = new BaseTagDecoder(json);
        return dec.decode();
    }

    // Filters
    public static String toJson(@NonNull Filters filters, Relay relay) throws NostrException {
        final var enc = new FiltersEncoder(filters, relay);
        return enc.encode();
    }

    public static String toJson(@NonNull Filters filters) throws NostrException {
        return Api.toJson(filters, null);
    }

    public static Filters fromJsonFilters(@NonNull String json) throws NostrException {
        final var dec = new FiltersDecoder(json);
        return dec.decode();
    }

    // Generic Tag Queries
    public static String toJson(@NonNull GenericTagQuery gtq, Relay relay) throws NostrException {
        final var enc = new GenericTagQueryEncoder(gtq, relay);
        return enc.encode();
    }

    public static String toJson(@NonNull GenericTagQuery gtq) throws NostrException {
        return Api.toJson(gtq, null);
    }

    // Utils
    private static Client createClient() {
        final var client = Client.getInstance();

        do {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        } while (client.getThreadPool().getCompletedTaskCount() < (client.getRelays().size() / 2));

        return client;
    }

}
