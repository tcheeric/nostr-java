package nostr.base;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public interface FEncoder<T> {
    ObjectMapper F_ENCODER_MAPPER_AFTERBURNER = JsonMapper.builder().addModule(new AfterburnerModule()).build().setSerializationInclusion(Include.NON_NULL);
    
    String encode();
}
