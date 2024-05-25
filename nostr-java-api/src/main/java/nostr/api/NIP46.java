package nostr.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import nostr.api.factory.impl.NIP46Impl;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.id.Identity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

public final class NIP46<T extends GenericEvent> extends EventNostr<T> {

    public NIP46(@NonNull Identity sender) {
        setSender(sender);
    }

    /**
     * Create an app request for the signer
     * @param request
     * @param signer
     * @return
     */
    public NIP46<T> createRequestEvent(@NonNull NIP46.Request request, @NonNull PublicKey signer) {
        var factory = new NIP46Impl.NostrConnectEventFactory(getSender(), request, signer);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    /**
     * @param response
     * @param app
     * @return
     */
    public NIP46<T> createResponseEvent(@NonNull NIP46.Response response, @NonNull PublicKey app) {
        var factory = new NIP46Impl.NostrConnectEventFactory(getSender(), response, app);
        var event = factory.create();
        setEvent((T) event);

        return this;
    }

    public interface NIP46ReqRes {
        String getId();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Log
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
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                // Handle the exception if needed
                log.log(Level.WARNING, "Error converting to JSON: {0}", ex.getMessage());
                return "{}"; // Return an empty JSON object as a fallback
            }
        }

        public static Request fromString(@NonNull String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonString, Request.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Log
    public static final class Response implements Serializable {
        private String id;
        private String error;
        private String result;

        public String toString() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                // Handle the exception if needed
                log.log(Level.WARNING, "Error converting to JSON: {0}", ex.getMessage());
                return "{}"; // Return an empty JSON object as a fallback
            }
        }

        public static Response fromString(@NonNull String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonString, Response.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
