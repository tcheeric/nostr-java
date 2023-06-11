package nostr.event.marshaller.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.IMarshaller;
import nostr.base.Relay;
import nostr.event.BaseTag;
import nostr.event.codec.CustomTagEncoder;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Data
@Builder
public class TagMarshaller implements IMarshaller {

    private final BaseTag tag;
    private final Relay relay;

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }

    private String toJson() throws NostrException {
        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(new CustomTagEncoder());
            var mapper = (new ObjectMapper())
                    .setSerializationInclusion(Include.NON_NULL)
                    .registerModule(module);

            return mapper.writeValueAsString(tag);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

}
