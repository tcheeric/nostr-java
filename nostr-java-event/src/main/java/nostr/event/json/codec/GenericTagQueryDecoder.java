//package nostr.event.json.codec;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.Data;
//import nostr.base.GenericTagQuery;
//
///**
// *
// * @author eric
// */
//@Data
//public class GenericTagQueryDecoder<T extends GenericTagQuery> implements FDecoder<T> {
//    private final Class<T> clazz;
//
//    public GenericTagQueryDecoder() {
//        this.clazz = (Class<T>) GenericTagQuery.class;
//    }
//
//    @Override
//    public T decode(String json) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            return mapper.readValue(json, clazz);
//        } catch (JsonProcessingException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//
//}
