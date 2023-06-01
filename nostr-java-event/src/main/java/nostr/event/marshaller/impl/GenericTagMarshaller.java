package nostr.event.marshaller.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.GenericTagQuery;
import nostr.base.IMarshaller;
import nostr.base.Relay;
import nostr.event.serializer.CustomGenericTagSerializer;
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

    @Override
    public String toJson() throws NostrException {
        try {
            SimpleModule module = new SimpleModule();
            module.addSerializer(new CustomGenericTagSerializer());
            var mappe = (new ObjectMapper())
                    .setSerializationInclusion(Include.NON_NULL)
                    .registerModule(module);

            return mappe.writeValueAsString(tag);
        } catch (JsonProcessingException e) {
            throw new NostrException(e);
        }
    }

}
