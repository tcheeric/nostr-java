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
import nostr.id.IIdentity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

public final class NIP46<T extends GenericEvent> extends EventNostr<T> {

    public NIP46(@NonNull IIdentity sender) {
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
        private String initiator;
        private String token;
        private LocalDateTime createdAt;
        private String requestUuid;
        @JsonIgnore
        private Method method;
        @JsonIgnore
        private Session session;
        private Set<Parameter> parameters = new LinkedHashSet<>();

        public void addParameter(Parameter parameter) {
            this.parameters.add(parameter);
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
        private Long id;
        private String responseUuid;
        private String result;
        @JsonIgnore
        private Method method;
        @JsonIgnore
        private Session session;
        private LocalDateTime createdAt;

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Log
    public static final class Method implements Serializable {
        private Long id;
        private String name;
        private String description;
        private Set<Request> requests = new LinkedHashSet<>();
        private Set<Response> responses = new LinkedHashSet<>();

        public void addRequest(@NonNull Request request) {
            this.requests.add(request);
        }

        public void addResponse(@NonNull Response response) {
            this.responses.add(response);
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

        public static Method fromString(@NonNull String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonString, Method.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Log
    public static final class Session implements Serializable {
        private Long id;
        private String sessionId;
        private String status;
        private String app;
        private String account;
        private LocalDateTime createdAt;
        private String token;
        private Set<Request> requests = new LinkedHashSet<>();
        private Set<Response> responses = new LinkedHashSet<>();

        public void addRequest(@NonNull Request request) {
            this.requests.add(request);
        }

        public void addResponse(@NonNull Response response) {
            this.responses.add(response);
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

        public static Session fromString(@NonNull String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonString, Session.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Log
    public static final class Parameter implements Serializable {
        private Long id;
        private String name;
        private String value;
        @JsonIgnore
        private Request request;

        public String toString() {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                log.log(Level.WARNING, "Error converting to JSON: {0}", ex.getMessage());
                return "{}"; // Return an empty JSON object as a fallback
            }
        }

        public static Parameter fromString(@NonNull String jsonString) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonString, Parameter.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
