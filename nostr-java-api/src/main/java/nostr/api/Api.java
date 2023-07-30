/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.base.GenericTagQuery;
import nostr.base.IElement;
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

    public static class Json {
        // Events

        public static String encode(@NonNull BaseEvent event) throws NostrException {
            return Api.Json.encode(event, null);
        }

        public static String encode(@NonNull BaseEvent event, Relay relay) throws NostrException {
            final var enc = new BaseEventEncoder(event, relay);
            return enc.encode();
        }

        public static BaseEvent decodeEvent(@NonNull String json) throws NostrException {
            final var dec = new BaseEventDecoder(json);
            return dec.decode();
        }

        // Messages
        public static String encode(@NonNull BaseMessage message, Relay relay) throws NostrException {
            final var enc = new BaseMessageEncoder(message, relay);
            return enc.encode();
        }

        public static String encode(@NonNull BaseMessage message) throws NostrException {
            return Api.Json.encode(message, null);
        }

        public static BaseMessage decodeMessage(@NonNull String json) throws NostrException {
            final var dec = new BaseMessageDecoder(json);
            return dec.decode();
        }

        // Tags
        public static String encode(@NonNull BaseTag tag, Relay relay) throws NostrException {
            final var enc = new BaseTagEncoder(tag, relay);
            return enc.encode();
        }

        public static String encode(@NonNull BaseTag tag) throws NostrException {
            return Api.Json.encode(tag, null);
        }

        public static BaseTag decodeTag(@NonNull String json) throws NostrException {
            final var dec = new BaseTagDecoder(json);
            return dec.decode();
        }

        // Filters
        public static String encode(@NonNull Filters filters, Relay relay) throws NostrException {
            final var enc = new FiltersEncoder(filters, relay);
            return enc.encode();
        }

        public static String encode(@NonNull Filters filters) throws NostrException {
            return Api.Json.encode(filters, null);
        }

        public static Filters decodeFilters(@NonNull String json) throws NostrException {
            final var dec = new FiltersDecoder(json);
            return dec.decode();
        }

        // Generic Tag Queries
        public static String encode(@NonNull GenericTagQuery gtq, Relay relay) throws NostrException {
            final var enc = new GenericTagQueryEncoder(gtq, relay);
            return enc.encode();
        }

        public static String encode(@NonNull GenericTagQuery gtq) throws NostrException {
            return Api.Json.encode(gtq, null);
        }

        public static IElement decode(@NonNull String json, @NonNull Class clazz) throws NostrException {
            switch (clazz.getName()) {
                case "nostr.event.BaseEvent.class" -> {
                    return decodeEvent(json);
                }
                case "nostr.event.BaseMessage.class" -> {
                    return decodeMessage(json);
                }
                case "nostr.event.BaseTag.class" -> {
                    return decodeTag(json);
                }
                case "nostr.event.Filters.class" -> {
                    return decodeFilters(json);
                }
                default -> throw new AssertionError();
            }
        }

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
