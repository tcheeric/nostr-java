/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.GenericTagQuery;
import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.base.Relay;
import nostr.client.Client;
import nostr.context.RequestContext;
import nostr.context.impl.DefaultRequestContext;
import nostr.crypto.schnorr.Schnorr;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersListEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.json.codec.GenericTagQueryEncoder;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import nostr.util.NostrUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author eric
 */
@NoArgsConstructor
public class Nostr implements NostrIF {

    private static Nostr INSTANCE;

    private Client client;
    @Getter
    private Identity sender;

    @Getter
    private Map<String, String> relays;

    public static NostrIF getInstance() {
        return (INSTANCE == null) ? new Nostr() : INSTANCE;
    }

    public static NostrIF getInstance(@NonNull Identity sender) {
        return (INSTANCE == null) ? new Nostr(sender) : INSTANCE;
    }

    public Nostr(@NonNull Identity sender) {
        this.sender = sender;
    }

    @Override
    public NostrIF setSender(@NonNull Identity sender) {
        this.sender = sender;

        return this;
    }

    @Override
    public NostrIF setRelays(@NonNull Map<String, String> relays) {
        this.relays = relays;

        return this;
    }

    @Override
    public void close() {
        if (client == null) {
            throw new IllegalStateException("Client is not initialized");
        }
        try {
            this.client.disconnect();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> send(@NonNull IEvent event) {
        return send(event, getRelays());
    }

    @Override
    public List<String> send(@NonNull IEvent event, Map<String, String> relays) {
        var context = new DefaultRequestContext();
        context.setPrivateKey(getSender().getPrivateKey().getRawData());
        context.setRelays(relays);

        return send(new EventMessage(event), context);
    }


    @Override
    public List<String> send(@NonNull Filters filters, @NonNull String subscriptionId) {
        return send(filters, subscriptionId, getRelays());
    }

    @Override
    public List<String> send(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
        List<Filters> filtersList = new ArrayList<>();
        filtersList.add(filters);

        return send(filtersList, subscriptionId, relays);
    }

    @Override
    public List<String> send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId) {
        return send(filtersList, subscriptionId, getRelays());
    }

    @Override
    public List<String> send(@NonNull List<Filters> filtersList, @NonNull String subscriptionId, Map<String, String> relays) {

        var context = new DefaultRequestContext();
        context.setRelays(relays);
        context.setPrivateKey(getSender().getPrivateKey().getRawData());
        var message = new ReqMessage(subscriptionId, filtersList);

        return send(message, context);
    }

    @Override
    public List<String> send(@NonNull BaseMessage message, @NonNull RequestContext context) {
        if (context instanceof DefaultRequestContext) {
            try {
                Client.getInstance().connect(context).send(message);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        return List.of();
    }

    /**
     * @param signable
     */
    @Override
    public NostrIF sign(@NonNull Identity identity, @NonNull ISignable signable) {
        identity.sign(signable);

        return this;
    }

    @Override
    public boolean verify(@NonNull GenericEvent event) {
        if (!event.isSigned()) {
            throw new IllegalStateException("The event is not signed");
        }

        var signature = event.getSignature();

        try {
            var message = NostrUtil.sha256(event.get_serializedEvent());
            return Schnorr.verify(message, event.getPubKey().getRawData(), signature.getRawData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> toMapRelays(List<Relay> relayList) {
        Map<String, String> relays = new HashMap<>();
        relayList.forEach(r -> relays.put(r.getName(), r.getUri()));
        return relays;
    }

    public static class Json {

        // Events

        /**
         * @param event
         */
        public static String encode(@NonNull BaseEvent event) {
            return Nostr.Json.encode(event, null);
        }

        /**
         * @param event
         * @param relay
         * @return
         */
        public static String encode(@NonNull BaseEvent event, Relay relay) {
            final var enc = new BaseEventEncoder(event);
            return enc.encode();
        }

        /**
         * @param json
         */
        public static GenericEvent decodeEvent(@NonNull String json) throws JsonProcessingException {
            return new GenericEventDecoder<>().decode(json);
        }

        // Messages

        /**
         * @param message
         * @param relay
         */
        public static String encode(@NonNull BaseMessage message, Relay relay) throws JsonProcessingException {
            return message.encode();
        }

        /**
         * @param message
         */
        public static String encode(@NonNull BaseMessage message) throws JsonProcessingException {
            return Nostr.Json.encode(message, null);
        }

        /**
         * @param json
         */
        public static BaseMessage decodeMessage(@NonNull String json) {
            return new BaseMessageDecoder().decode(json);
        }

        // Tags

        /**
         * @param tag
         * @param relay
         */
        public static String encode(@NonNull BaseTag tag, Relay relay) {
            final var enc = new BaseTagEncoder(tag, relay);
            return enc.encode();
        }

        /**
         * @param tag
         */
        public static String encode(@NonNull BaseTag tag) {
            return Nostr.Json.encode(tag, null);
        }

        /**
         * @param json
         */
        public static BaseTag decodeTag(@NonNull String json) {
            return new BaseTagDecoder<>().decode(json);
        }

        // Filters

        /**
         * @param filtersList
         * @param relay
         */
        public static String encode(@NonNull List<Filters> filtersList, Relay relay) {
            final var enc = new FiltersListEncoder(filtersList);
            return enc.encode();
        }

        /**
         * @param filtersList
         */
        public static String encode(@NonNull List<Filters> filtersList) {
            return Nostr.Json.encode(filtersList, null);
        }

        /**
         * @param json
         */
        public static Filters decodeFilters(@NonNull String json) {
            return new FiltersDecoder<>().decode(json);
        }

        // Generic Tag Queries

        /**
         * @param gtq
         * @param relay
         */
        public static String encode(@NonNull GenericTagQuery gtq, Relay relay) {
            final var enc = new GenericTagQueryEncoder(gtq, relay);
            return enc.encode();
        }

        /**
         * @param gtq
         */
        public static String encode(@NonNull GenericTagQuery gtq) {
            return Nostr.Json.encode(gtq, null);
        }

        /**
         * @param json
         * @param clazz
         */
        public static IElement decode(@NonNull String json, @NonNull Class clazz) throws JsonProcessingException {
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
                default -> throw new AssertionError();
            }
        }

        public static Filters decodeFilters(@NonNull String json, @NonNull Class clazz) {
            switch (clazz.getName()) {
                case "nostr.event.Filters.class" -> {
                    return decodeFilters(json);
                }
                default -> throw new AssertionError();
            }
        }

    }
}
