//package nostr.event.json.codec;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.Data;
//import nostr.event.impl.Filters;
//
//@Data
//public class FiltersListDecoder<T extends Filters> implements FDecoder<T> {
//    private final Class<T> clazz;
//
//    public FiltersListDecoder() {
//        this.clazz = (Class<T>)Filters.class;
//    }
//
//    @Override
//    public T decode(String jsonString)  {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            return mapper.readValue(jsonString, clazz);
//        } catch (JsonProcessingException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//}
