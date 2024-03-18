/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.base.GenericTagQuery;
import nostr.base.IElement;
import nostr.base.IEvent;
import nostr.base.ISignable;
import nostr.base.Relay;
import nostr.client.Client;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.json.codec.BaseMessageEncoder;
import nostr.event.json.codec.BaseTagDecoder;
import nostr.event.json.codec.BaseTagEncoder;
import nostr.event.json.codec.FiltersDecoder;
import nostr.event.json.codec.FiltersEncoder;
import nostr.event.json.codec.GenericEventDecoder;
import nostr.event.json.codec.GenericTagQueryEncoder;
import nostr.id.IIdentity;

import java.util.List;
import java.util.Map;

/**
 * @author eric
 */
@NoArgsConstructor
public class Nostr {

	private static Nostr INSTANCE;

	private Client client;
	@Getter
	private IIdentity sender;

	public static Nostr getInstance() {
		return (INSTANCE == null) ? new Nostr() : INSTANCE;
	}

    public static Nostr getInstance(@NonNull IIdentity sender) {
        return (INSTANCE == null) ? new Nostr(sender) : INSTANCE;
    }

    public Nostr(@NonNull IIdentity sender) {
        this.sender = sender;
    }
	
	public Nostr setSender(@NonNull IIdentity sender) {
		this.sender = sender;
		
		return this;
	}

	public Nostr setRelays(Map<String, String> relays) {
		this.client = Client.getInstance(relays);

		return this;
	}

	protected Client getClient() {
		client = (client == null) ? Client.getInstance() : client;

		return client;
	}
	
	public List<BaseMessage> responses(){
		return getClient().getResponses();
	}

	public void send(@NonNull IEvent event) {
		getClient().send(event);
    }

	public void send(@NonNull Filters filters, @NonNull String subscriptionId) {
		getClient().send(filters, subscriptionId);
    }

    public void send(@NonNull Filters filters, @NonNull String subscriptionId, Map<String, String> relays) {
        setRelays(relays);
        getClient().send(filters, subscriptionId);
    }

    /**
     * @param signable
     */
	public Nostr sign(@NonNull IIdentity identity, @NonNull ISignable signable) {
		identity.sign(signable);

		return this;
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
            final var enc = new BaseEventEncoder(event, relay);
            return enc.encode();
        }

        /**
         * @param json
         */
        public static GenericEvent decodeEvent(@NonNull String json) {
            final var dec = new GenericEventDecoder(json);
            return dec.decode();
        }

        // Messages

        /**
         * @param message
         * @param relay
         */
        public static String encode(@NonNull BaseMessage message, Relay relay) {
            final var enc = new BaseMessageEncoder(message, relay);
            return enc.encode();
        }

        /**
         * @param message
         */
        public static String encode(@NonNull BaseMessage message) {
            return Nostr.Json.encode(message, null);
        }

        /**
         * @param json
         */
        public static BaseMessage decodeMessage(@NonNull String json) {
            final var dec = new BaseMessageDecoder(json);
            return dec.decode();
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
            final var dec = new BaseTagDecoder(json);
            return dec.decode();
        }

        // Filters

        /**
         * @param filters
         * @param relay
         */
        public static String encode(@NonNull Filters filters, Relay relay) {
            final var enc = new FiltersEncoder(filters, relay);
            return enc.encode();
        }

        /**
         * @param filters
         */
        public static String encode(@NonNull Filters filters) {
            return Nostr.Json.encode(filters, null);
        }

        /**
         * @param json
         */
        public static Filters decodeFilters(@NonNull String json) {
            final var dec = new FiltersDecoder(json);
            return dec.decode();
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
        public static IElement decode(@NonNull String json, @NonNull Class clazz) {
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
}
