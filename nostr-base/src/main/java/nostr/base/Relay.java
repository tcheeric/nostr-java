package nostr.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author squirrel
 */
@Builder
@Data
@ToString
public class Relay {

    private final String uri;

    @ToString.Exclude
    @Builder.Default
    private RelayInformationDocument informationDocument = new RelayInformationDocument();

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelayInformationDocument {

        private String name;

        private String description;

        private String pubkey;

        private String contact;

        @Builder.Default
        private List<Integer> supportedNips = new ArrayList<>();

        @Builder.Default
        private List<String> supportedNipExtensions = new ArrayList<>();

        private String software;

        private String version;

        private Limitation limitation;

        private String paymentsUrl;

        private Fees fees;

        @Data
        public static class Limitation {

            @JsonProperty("max_message_length")
            private int maxMessageLength;

            @JsonProperty("max_subscriptions")
            private int maxSubscriptions;

            @JsonProperty("max_filters")
            private int maxFilters;

            @JsonProperty("max_limit")
            private int maxLimit;

            @JsonProperty("max_subid_length")
            private int maxSubIdLength;

            @JsonProperty("min_prefix")
            private int minPrefix;

            @JsonProperty("max_event_tags")
            private int maxEventTags;

            @JsonProperty("max_content_length")
            private int maxContentLength;

            @JsonProperty("min_pow_difficulty")
            private int minPowDifficulty;

            @JsonProperty("auth_required")
            private boolean authRequired;

            @JsonProperty("payment_required")
            private boolean paymentRequired;

        }

        @Data
        public static class Fees {

            private List<AdmissionFee> admission;
            private List<PublicationFee> publication;

            @Data
            public static class AdmissionFee {

                private int amount;
                private String unit;
            }

            @Data
            public static class PublicationFee {

                private int amount;
                private String unit;
            }
        }

    }
}
