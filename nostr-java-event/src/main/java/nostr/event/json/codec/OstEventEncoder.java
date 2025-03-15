//package nostr.event.json.codec;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import java.util.HashMap;
//import lombok.AllArgsConstructor;
//
//import lombok.Data;
//import nostr.base.ElementAttribute;
//import nostr.base.IEncoder;
//import nostr.base.Relay;
//import nostr.event.impl.OtsEvent;
//import nostr.util.NostrException;
//
///**
// * @author guilhermegps
// *
// */
//@Data
//@AllArgsConstructor
//public class OstEventEncoder implements IEncoder<OtsEvent> {
//
//    private final OtsEvent event;
//    private final Relay relay;
//
//    public OstEventEncoder(OtsEvent event) {
//        this(event, null);
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
//    private String toJson() throws NostrException {
//        try {
//            JsonNode node = I_ENCODER_MAPPER_AFTERBURNER.valueToTree(event);
//            ObjectNode objNode = (ObjectNode) node;
//            event.getAttributes().parallelStream()
//                    .map(ElementAttribute::getValue)
//                    .forEach(ev -> {
//                        var expression = (HashMap<String, String>) ev;
//
//                        objNode.set("ots", I_ENCODER_MAPPER_AFTERBURNER.valueToTree(expression.get("ots")));
//                    });
//
//            return I_ENCODER_MAPPER_AFTERBURNER.writeValueAsString(node);
//        } catch (JsonProcessingException | IllegalArgumentException e) {
//            throw new NostrException(e);
//        }
//    }
//
//}
