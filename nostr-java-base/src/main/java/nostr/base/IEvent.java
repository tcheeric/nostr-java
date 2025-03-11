package nostr.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 *
 * @author squirrel
 */
public interface IEvent extends IElement, IBech32Encodable {
    ObjectMapper MAPPER_AFTERBURNER = JsonMapper.builder().addModule(new AfterburnerModule()).build();
    String getId();
}
