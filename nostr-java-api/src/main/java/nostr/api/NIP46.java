package nostr.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.api.factory.impl.NIP28Impl;
import nostr.api.factory.impl.NIP46Impl;
import nostr.api.factory.impl.NIP46Impl.NostrConnectEventFactory;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.impl.NostrConnectEvent;
import nostr.id.IIdentity;
import nostr.id.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public NIP46<T> createRequestEvent(@NonNull NIP46.NIP46Request request, @NonNull PublicKey signer) {
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
    public NIP46<T> createResponseEvent(@NonNull NIP46.NIP46Response response, @NonNull PublicKey app) {
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
    public static final class NIP46Request implements NIP46ReqRes {
        private String id;
        private String method;
        private List<String> params;
        private String sessionId;

        public NIP46Request(@NonNull String method) {
            this(UUID.randomUUID().toString(), method, new ArrayList<>(), null);
        }

        @Override
        public String toString() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                // Handle the exception if needed
                ex.printStackTrace();
                return "{}"; // Return an empty JSON object as a fallback
            }
        }

        public static NIP46Request fromString(@NonNull String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonString, NIP46Request.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class NIP46Response implements NIP46ReqRes {
        private String id;
        private String method;
        private Object result;
        private String error;
        private String sessionId;

        public NIP46Response(@NonNull String id, @NonNull String method, @NonNull String result) {
            this(id, method, result, null, null);
        }

        @Override
        public String toString() {
            try {
                var objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                // Handle the exception if needed
                ex.printStackTrace();
                return "{}"; // Return an empty JSON object as a fallback
            }
        }

        public static NIP46Response fromString(@NonNull String jsonString) {
            try {
                var objectMapper = new ObjectMapper();
                return objectMapper.readValue(jsonString, NIP46Response.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
