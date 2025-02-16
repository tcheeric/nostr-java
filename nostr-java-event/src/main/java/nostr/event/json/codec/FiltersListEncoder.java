//package nostr.event.json.codec;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import lombok.Data;
//import nostr.base.FEncoder;
//import nostr.util.NostrException;
//
//import java.util.List;
//
//@Data
//public class FiltersListEncoder implements FEncoder<Filters> {
//
//    private final List<Filters> filtersList;
//
//    public FiltersListEncoder(List<Filters> filtersList) {
//        this.filtersList = filtersList;
//    }
//
//    @Override
//    public String encode() {
//        try {
//            return toJson();
//        } catch (NostrException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//    protected String toJson() throws NostrException {
//        return toJsonCommaSeparated();
//    }
//
//    private String toJsonArray() throws NostrException {
//        try {
//            StringBuilder sb = new StringBuilder();
//            for (Object filter : getFiltersList()) {
//                if (!sb.isEmpty()) {
//                    sb.append(",");
//                }
//                sb.append(MAPPER.writeValueAsString(filter));
//            }
//            return sb.toString();
//        } catch (JsonProcessingException | IllegalArgumentException e) {
//            throw new NostrException(e);
//        }
//    }
//
//    private String toJsonCommaSeparated() throws NostrException {
//        JsonNode node = MAPPER.valueToTree(getFiltersList());
//        try {
//            return MAPPER.writeValueAsString(node);
//        } catch (JsonProcessingException e) {
//            throw new NostrException(e);
//        }
//
//    }
//}
