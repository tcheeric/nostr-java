package nostr.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

/**
 *
 * @author squirrel
 */
public interface IEvent extends IElement, IBech32Encodable {
    ObjectMapper MAPPER_AFTERBURNER = JsonMapper.builder().addModule(new BlackbirdModule()).build();
    String getId();
}
