package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface FEncoder<T> {
    ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    
    String encode();
}
