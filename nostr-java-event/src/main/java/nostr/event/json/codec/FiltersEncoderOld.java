//package nostr.event.json.codec;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import nostr.base.FEncoder;
//import nostr.util.NostrException;
//
//import java.util.Spliterator;
//import java.util.Spliterators;
//import java.util.stream.StreamSupport;
//
///**
// * @author guilhermegps
// */
//@Data
//@EqualsAndHashCode(callSuper = false)
//public class FiltersEncoder implements FEncoder<Filters> {
//    private final Filters filters;
//
//    public FiltersEncoder(Filters filters) {
//        this.filters = filters;
//    }
//
//    protected String toJson() throws NostrException {
//        try {
//            JsonNode node = MAPPER.valueToTree(filters);
//            ObjectNode objNode = (ObjectNode) node;
//            //var arrayNode = (ArrayNode) node.get("genericTagQuery");
//            if (objNode != null && !objNode.isNull()) {
//                for (JsonNode jn : objNode) {
//                    StreamSupport.stream(
//                            Spliterators.spliteratorUnknownSize(jn.fields(), Spliterator.ORDERED), false)
//                        .forEach(f -> {
//                            if ("genericTagQuery".equals(f.getKey())) {
//                                var mapper = new ObjectMapper();
//                                try {
//                                    mapper.readTree(f.getValue().toString())
//                                        .fields()
//                                        .forEachRemaining(g -> objNode.set(g.getKey(), g.getValue()));
//                                } catch (JsonProcessingException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            } else {
//                                objNode.set(f.getKey(), f.getValue());
//                            }
//                        });
//                }
//            }
//            objNode.remove("genericTagQuery");
//
//            return MAPPER.writeValueAsString(objNode);
//        } catch (JsonProcessingException | IllegalArgumentException e) {
//            throw new NostrException(e);
//        }
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
//}
