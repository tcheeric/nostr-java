package nostr.event.marshaller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.IMarshaller;
import nostr.base.Relay;
import nostr.event.impl.Filters;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Data
@Builder
public class FilterMarshaller implements IMarshaller {

    private final Filters filters;
    private final Relay relay;

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }

    private String toJson() throws NostrException {
        try {
            JsonNode node = MAPPER.valueToTree(filters);
            ObjectNode objNode = (ObjectNode) node;
            var arrayNode = (ArrayNode) node.get("genericTagQueryList");
            if (arrayNode != null && !arrayNode.isNull()) {
                for (JsonNode jn : arrayNode) {
                    StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(jn.fields(), Spliterator.ORDERED), false)
                            .forEach(f -> {
                                objNode.set(f.getKey(), f.getValue());
                            });
                }
            }
            objNode.remove("genericTagQueryList");

            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new NostrException(e);
        }
    }

}
