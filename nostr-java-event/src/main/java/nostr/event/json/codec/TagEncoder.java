package nostr.event.json.codec;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.json.serializer.TagSerializer;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TagEncoder extends ElementEncoder {

    public TagEncoder(BaseTag tag, Relay relay) {
        super(tag, relay);
    }

    public TagEncoder(BaseTag tag) {
        super(tag);
    }        

    @Override
    protected String toJson() throws NostrException {
        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(new TagSerializer());
            var mapper = (new ObjectMapper())
                    .setSerializationInclusion(Include.NON_NULL)
                    .registerModule(module);

            return mapper.writeValueAsString((BaseTag) getElement());
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

}
