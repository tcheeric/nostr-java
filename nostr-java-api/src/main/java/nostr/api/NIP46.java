package nostr.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import nostr.api.factory.impl.NIP46.NostrConnectEventFactory;
import nostr.base.PublicKey;
import nostr.event.impl.NostrConnectEvent;
import nostr.id.IIdentity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public final class NIP46 extends Nostr {

    /**
     * Create an app request for the signer
     *
     * @param request
     * @param signer
     * @return
     */
    public static NostrConnectEvent createRequestEvent(@NonNull NIP46.Request request, @NonNull IIdentity sender, PublicKey signer) {
        return new NostrConnectEventFactory(request, sender, signer).create();
    }

    /**
     * @param response
     * @param app
     * @return
     */
    public static NostrConnectEvent createResponseEvent(@NonNull NIP46.Response response, @NonNull IIdentity sender, @NonNull PublicKey app) {
        return new NostrConnectEventFactory(response, sender, app).create();
    }

    public interface NIP46ReqRes {
        String getId();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
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
                ex.printStackTrace();
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
                ex.printStackTrace();
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
                ex.printStackTrace();
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
                ex.printStackTrace();
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
                // Handle the exception if needed
                ex.printStackTrace();
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Deprecated(forRemoval = true)
    public static final class NIP46Request implements NIP46ReqRes {
        private String id;
        private String method;
        private List<String> params;
        private String jwt;

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
    @Deprecated(forRemoval = true)
    public static final class NIP46Response implements NIP46ReqRes {
        private String id;
        private String method;
        private Object result;
        private String jwt;

        public NIP46Response(@NonNull String id, @NonNull String method, @NonNull String result) {
            this(id, method, result, null);
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
