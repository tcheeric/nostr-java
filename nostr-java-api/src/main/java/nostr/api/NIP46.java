package nostr.api;

import static nostr.base.IEvent.MAPPER_BLACKBIRD;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.GenericEventFactory;
import nostr.base.PublicKey;
import nostr.config.Constants;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

@Slf4j
/**
 * NIP-46 helpers (Nostr Connect). Build app requests and signer responses.
 * Spec: https://github.com/nostr-protocol/nips/blob/master/46.md
 */
public final class NIP46 extends EventNostr {

  public NIP46(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create an app request for the signer
   *
   * @param request the request payload (RPC-like) serialized to JSON
   * @param signer the target signer public key
   * @return this instance for chaining
   */
  public NIP46 createRequestEvent(@NonNull NIP46.Request request, @NonNull PublicKey signer) {
    String content = NIP44.encrypt(getSender(), request.toString(), signer);
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.REQUEST_EVENTS, content).create();
    genericEvent.addTag(NIP01.createPubKeyTag(signer));
    this.updateEvent(genericEvent);
    return this;
  }

  /**
   * Create a signer response for the app.
   *
   * @param response the response payload serialized to JSON
   * @param app the target app public key
   * @return this instance for chaining
   */
  public NIP46 createResponseEvent(@NonNull NIP46.Response response, @NonNull PublicKey app) {
    String content = NIP44.encrypt(getSender(), response.toString(), app);
    GenericEvent genericEvent =
        new GenericEventFactory(getSender(), Constants.Kind.REQUEST_EVENTS, content).create();
    genericEvent.addTag(NIP01.createPubKeyTag(app));
    this.updateEvent(genericEvent);
    return this;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Slf4j
  public static final class Request implements Serializable {
    private String id;
    private String method;
    // @JsonIgnore
    private Set<String> params = new LinkedHashSet<>();

    /**
     * Add a parameter to the request payload preserving insertion order.
     *
     * @param param the parameter value
     */
    public void addParam(String param) {
      this.params.add(param);
    }

    /**
     * Serialize this request to JSON.
     */
    public String toString() {
      try {
        return MAPPER_BLACKBIRD.writeValueAsString(this);
      } catch (JsonProcessingException ex) {
        log.warn("Error converting request to JSON: {}", ex.getMessage());
        return "{}"; // Return an empty JSON object as a fallback
      }
    }

    /**
     * Deserialize a JSON string into a Request.
     *
     * @param jsonString the JSON string
     * @return the parsed Request
     */
    public static Request fromString(@NonNull String jsonString) {
      try {
        return MAPPER_BLACKBIRD.readValue(jsonString, Request.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Slf4j
  public static final class Response implements Serializable {
    private String id;
    private String error;
    private String result;

    /**
     * Serialize this response to JSON.
     */
    public String toString() {
      try {
        return MAPPER_BLACKBIRD.writeValueAsString(this);
      } catch (JsonProcessingException ex) {
        log.warn("Error converting response to JSON: {}", ex.getMessage());
        return "{}"; // Return an empty JSON object as a fallback
      }
    }

    /**
     * Deserialize a JSON string into a Response.
     *
     * @param jsonString the JSON string
     * @return the parsed Response
     */
    public static Response fromString(@NonNull String jsonString) {
      try {
        return MAPPER_BLACKBIRD.readValue(jsonString, Response.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
