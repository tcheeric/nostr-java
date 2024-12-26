//package nostr.event.json.serializer;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.fasterxml.jackson.databind.ser.std.StdSerializer;
//import nostr.base.GenericTagQuery;
//import nostr.base.IEncoder;
//import nostr.event.BaseTag;
//
//import java.io.IOException;
//import java.io.Serial;
//import java.util.Map;
//
///**
// * @author guilhermegps
// */
//public class CustomGenericTagQuerySerializer<T extends Map<String,Object>> extends StdSerializer<T> {
//
//    @Serial
//    private static final long serialVersionUID = 6803478463890319884L;
//
//    protected CustomGenericTagQuerySerializer() {
//        super((Class) Map.class);
//    }
//
//    @Override
//    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//        var mapper = IEncoder.MAPPER;
//        JsonNode node = mapper.valueToTree(value);
//        ObjectNode objNode = (ObjectNode) node;
////        String key = value.entrySet().stream().findFirst().get().getKey();
////        String attrName = "#" + key;
////        objNode.set(attrName, node.get(key));
////        objNode.remove(key);
//        gen.writeTree(objNode);
//    }
//}
