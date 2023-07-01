package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.Relay;
import nostr.event.impl.Filters;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FiltersEncoder extends BaseEventEncoder {

    public FiltersEncoder(Filters filters, Relay relay) {
        super(filters, relay);
    }

    public FiltersEncoder(Filters filters) {
        super(filters);
    }

    @Override
    protected String toJson() throws NostrException {
        try {
            JsonNode node = MAPPER.valueToTree((Filters) getEvent());
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
