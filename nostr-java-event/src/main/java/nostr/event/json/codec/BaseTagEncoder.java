package nostr.event.json.codec;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NonNull;
import nostr.base.IEncoder;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.json.serializer.TagSerializer;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Data
@AllArgsConstructor
public class BaseTagEncoder implements IEncoder<BaseTag> {

    private final BaseTag tag;
    private final Relay relay;

    public BaseTagEncoder(@NonNull BaseTag tag) {
        this(tag, null);
    }

    @Override
    public String encode() throws NostrException {
        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(new TagSerializer());
            var mapper = (new ObjectMapper())
                    .setSerializationInclusion(Include.NON_NULL)
                    .registerModule(module);

            return mapper.writeValueAsString(tag);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }
}
