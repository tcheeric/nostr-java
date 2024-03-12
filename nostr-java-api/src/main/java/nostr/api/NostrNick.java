/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nostr.api;

import lombok.NonNull;
import nostr.base.*;
import nostr.client.Client;
import nostr.event.BaseEvent;
import nostr.event.BaseMessage;
import nostr.event.BaseTag;
import nostr.event.impl.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.*;
import nostr.id.IIdentity;
import nostr.id.Identity;

/**
 * @author eric
 */
public abstract class NostrNick {

  /**
   * @param event
   */
  public static void send(@NonNull IEvent event) {
    var client = createClient();
    client.send(event);
  }

  public static void send(@NonNull Filters filters, @NonNull String subscriptionId) {
    var client = createClient();
    client.send(filters, subscriptionId);
  }

  /**
   * @param signable
   * @return
   */

  public static Signature sign(@NonNull IIdentity identity, @NonNull ISignable signable) {
    return identity.sign(signable);
  }

  public static Signature sign(@NonNull ISignable signable) {
    Identity identity = Identity.getInstance();
    return identity.sign(signable);
  }

  /**
   *
   */
  public static class Json {

    // Events

    /**
     * @param event
     * @return
     */
    public static String encode(@NonNull BaseEvent event) {
      return NostrNick.Json.encode(event, null);
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
     * @return
     */
    public static GenericEvent decodeEvent(@NonNull String json) {
      final var dec = new GenericEventDecoder(json);
      return dec.decode();
    }

    // Messages

    /**
     * @param message
     * @param relay
     * @return
     */
    public static String encode(@NonNull BaseMessage message, Relay relay) {
      final var enc = new BaseMessageEncoder(message, relay);
      return enc.encode();
    }

    /**
     * @param message
     * @return
     */
    public static String encode(@NonNull BaseMessage message) {
      return NostrNick.Json.encode(message, null);
    }

    /**
     * @param json
     * @return
     */
    public static BaseMessage decodeMessage(@NonNull String json) {
      final var dec = new BaseMessageDecoder(json);
      return dec.decode();
    }

    // Tags

    /**
     * @param tag
     * @param relay
     * @return
     */
    public static String encode(@NonNull BaseTag tag, Relay relay) {
      final var enc = new BaseTagEncoder(tag, relay);
      return enc.encode();
    }

    /**
     * @param tag
     * @return
     */
    public static String encode(@NonNull BaseTag tag) {
      return NostrNick.Json.encode(tag, null);
    }

    /**
     * @param json
     * @return
     */
    public static BaseTag decodeTag(@NonNull String json) {
      final var dec = new BaseTagDecoder(json);
      return dec.decode();
    }

    // Filters

    /**
     * @param filters
     * @param relay
     * @return
     */
    public static String encode(@NonNull Filters filters, Relay relay) {
      final var enc = new FiltersEncoder(filters, relay);
      return enc.encode();
    }

    /**
     * @param filters
     * @return
     */
    public static String encode(@NonNull Filters filters) {
      return NostrNick.Json.encode(filters, null);
    }

    /**
     * @param json
     * @return
     */
    public static Filters decodeFilters(@NonNull String json) {
      final var dec = new FiltersDecoder(json);
      return dec.decode();
    }

    // Generic Tag Queries

    /**
     * @param gtq
     * @param relay
     * @return
     */
    public static String encode(@NonNull GenericTagQuery gtq, Relay relay) {
      final var enc = new GenericTagQueryEncoder(gtq, relay);
      return enc.encode();
    }

    /**
     * @param gtq
     * @return
     */
    public static String encode(@NonNull GenericTagQuery gtq) {
      return NostrNick.Json.encode(gtq, null);
    }

    /**
     * @param json
     * @param clazz
     * @return
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

  // Utils

  /**
   * @return
   */
  protected static Client createClient() {

    return Client.getInstance();
  }

}
