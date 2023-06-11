package nostr.event.marshaller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.GenericTagQuery;
import nostr.base.IMarshaller;
import nostr.base.Relay;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Data
@Builder
public class GenericTagMarshaller implements IMarshaller {

    private final GenericTagQuery tag;
    private final Relay relay;

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }

    private String toJson() throws NostrException {
        try {
//            SimpleModule module = new SimpleModule();
//            module.addSerializer(new CustomGenericTagSerializer());
//            var mapper = (new ObjectMapper())
//                    .setSerializationInclusion(Include.NON_NULL)
//            MAPPER.registerModule(module);

            return MAPPER.writeValueAsString(tag);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

}
