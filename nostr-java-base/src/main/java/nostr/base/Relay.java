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

import java.util.ArrayList;
import java.util.List;

/**
 * @author squirrel
 */
// @Builder
@Data
@EqualsAndHashCode
@AllArgsConstructor
@Log
public class Relay {

    @EqualsAndHashCode.Include
    @ToString.Include
    private String scheme;

    @EqualsAndHashCode.Include
    @ToString.Include
    private String host;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RelayInformationDocument informationDocument;

    public Relay(@NonNull String uri) {
        this(uri, new RelayInformationDocument());
    }

    public Relay(@NonNull String uri, RelayInformationDocument doc) {
        this.informationDocument = doc;
        String[] s = uri.split("://", 2);
        if (s.length != 2) {
            throw new RuntimeException("uri must contain scheme and host, passed in uri: \"" + uri + "\"");
        }
        this.scheme = s[0];
        this.host = s[1];
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

    // Helper method
    public String getUri() {
        return this.scheme + "://" + this.host;
    }

    // Helper method
    public String getHttpUri() {
        return this.scheme.replaceFirst("ws", "http") + "://" + this.host;
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
