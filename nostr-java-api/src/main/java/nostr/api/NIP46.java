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
public final class NIP46 extends EventNostr {

  public NIP46(@NonNull Identity sender) {
    setSender(sender);
  }

  /**
   * Create an app request for the signer
   *
   * @param request
   * @param signer
   * @return
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
   * @param response
   * @param app
   * @return
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

    public void addParam(String param) {
      this.params.add(param);
    }

    public String toString() {
      try {
        return MAPPER_BLACKBIRD.writeValueAsString(this);
      } catch (JsonProcessingException ex) {
        log.warn("Error converting request to JSON: {}", ex.getMessage());
        return "{}"; // Return an empty JSON object as a fallback
      }
    }

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

    public String toString() {
      try {
        return MAPPER_BLACKBIRD.writeValueAsString(this);
      } catch (JsonProcessingException ex) {
        log.warn("Error converting response to JSON: {}", ex.getMessage());
        return "{}"; // Return an empty JSON object as a fallback
      }
    }

    public static Response fromString(@NonNull String jsonString) {
      try {
        return MAPPER_BLACKBIRD.readValue(jsonString, Response.class);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
