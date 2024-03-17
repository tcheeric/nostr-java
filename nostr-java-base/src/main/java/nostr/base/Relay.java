package nostr.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.java.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author squirrel
 */
//@Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
@Log
public class Relay {

    public static final String PROTOCOL_WS = "ws";
    public static final String PROTOCOL_WSS = "wss";
    public static final Integer DEFAULT_PORT = 80;
    public static final Integer DEFAULT_SECURE_PORT = 443;

    private String scheme;

    @EqualsAndHashCode.Include
    private final String hostname;

    private int port;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RelayInformationDocument informationDocument;


    public Relay(@NonNull String hostname) {
        this(null, hostname, DEFAULT_PORT, new RelayInformationDocument());
    }

    public Relay(@NonNull String hostname, @NonNull RelayInformationDocument relayInformationDocument) {
        this(null, hostname, DEFAULT_PORT, relayInformationDocument);
    }

    public Relay(@NonNull String scheme, @NonNull String hostname) {
        this(scheme, hostname, DEFAULT_PORT, new RelayInformationDocument());
    }

    public Relay(@NonNull String scheme, @NonNull String hostname, int port) {
        this(scheme, hostname, port, new RelayInformationDocument());
    }

    public URI getURI() {
        try {
            return new URI(toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method
    public List<Integer> getSupportedNips() {
        return this.getInformationDocument().getSupportedNips();
    }

    // Helper method
    public void addNipSupport(int nip) {
        this.getSupportedNips().add(nip);
    }

    // Helper method
    public String printSupportedNips() {
        return convertToJsonArray(this.getInformationDocument().getSupportedNips());
    }

    private static String convertToJsonArray(List<Integer> list) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (int i = 0; i < list.size(); i++) {
            jsonBuilder.append("\"").append(list.get(i)).append("\"");
            if (i != list.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");

        return jsonBuilder.toString();
    }

    // Helper method
    public String getName() {
        return this.getInformationDocument().getName();
    }

    public static Relay fromString(@NonNull String address) {
        // Split the address into parts based on ":"
        String[] parts = address.split(":");

        String scheme;
        String hostname;
        int port;

        // Check if there are at least two parts (scheme and hostname)
        if (parts.length >= 2) {
            scheme = parts[0].trim();
            hostname = parts[1].trim().substring(2);
            port = getDefaultPort(scheme);

            // If there's a third part, parse it as the port
            if (parts.length >= 3) {
                try {
                    port = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException e) {
                    port = getDefaultPort(scheme);
                }
            }
        } else {
            // Handle the case where there are not enough parts in the address
            // You can choose to throw an exception or handle it differently based on your requirements.
            throw new IllegalArgumentException("Invalid address format: " + address);
        }

        return new Relay(scheme, hostname, port, new RelayInformationDocument());
    }

    @Override
    public String toString() {
        return scheme + "://" + hostname + ":" + port;
    }

    private static int getDefaultPort(@NonNull String scheme) {
        return scheme.equals("wss") ? DEFAULT_SECURE_PORT : scheme.equals("ws") ? DEFAULT_PORT : -1;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelayInformationDocument {

        @JsonProperty
        private String name;

        @JsonProperty
        private String description;

        @JsonProperty
        private String pubkey;

        @JsonProperty
        @JsonIgnoreProperties(ignoreUnknown = true)
        private String id;

        @JsonProperty
        @JsonIgnoreProperties(ignoreUnknown = true)
        private String contact;

        @Builder.Default
        @JsonProperty("supported_nips")
        @JsonIgnoreProperties(ignoreUnknown = true)
        private List<Integer> supportedNips = new ArrayList<>();

        @Builder.Default
        @JsonProperty("supported_nip_extensions")
        @JsonIgnoreProperties(ignoreUnknown = true)
        private List<String> supportedNipExtensions = new ArrayList<>();

        @JsonProperty
        @JsonIgnoreProperties(ignoreUnknown = true)
        private String software;

        @JsonProperty
        @JsonIgnoreProperties(ignoreUnknown = true)
        private String version;

        @JsonProperty
        @JsonIgnoreProperties(ignoreUnknown = true)
        private Limitation limitation;

        @JsonProperty("payments_url")
        private String paymentsUrl;

        @JsonProperty
        @JsonIgnoreProperties(ignoreUnknown = true)
        private Fees fees;

        @Data
        public static class Limitation {

            @JsonProperty("max_message_length")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxMessageLength;

            @JsonProperty("max_subscriptions")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxSubscriptions;

            @JsonProperty("max_filters")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxFilters;

            @JsonProperty("max_limit")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxLimit;

            @JsonProperty("max_subid_length")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxSubIdLength;

            @JsonProperty("min_prefix")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int minPrefix;

            @JsonProperty("max_event_tags")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxEventTags;

            @JsonProperty("max_content_length")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int maxContentLength;

            @JsonProperty("min_pow_difficulty")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private int minPowDifficulty;

            @JsonProperty("auth_required")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private boolean authRequired;

            @JsonProperty("payment_required")
            @JsonIgnoreProperties(ignoreUnknown = true)
            private boolean paymentRequired;

        }

        @Data
        public static class Fees {

            @JsonProperty
            @JsonIgnoreProperties(ignoreUnknown = true)
            private List<AdmissionFee> admission;

            @JsonProperty
            @JsonIgnoreProperties(ignoreUnknown = true)
            private List<PublicationFee> publication;

            @Data
            public static class AdmissionFee {

                @JsonProperty
                @JsonIgnoreProperties(ignoreUnknown = true)
                private int amount;

                @JsonProperty
                @JsonIgnoreProperties(ignoreUnknown = true)
                private String unit;
            }

            @Data
            public static class PublicationFee {

                @JsonProperty
                @JsonIgnoreProperties(ignoreUnknown = true)
                private int amount;

                @JsonProperty
                @JsonIgnoreProperties(ignoreUnknown = true)
                private String unit;
            }
        }
    }
}
