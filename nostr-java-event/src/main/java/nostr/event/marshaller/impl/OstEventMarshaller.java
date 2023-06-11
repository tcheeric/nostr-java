package nostr.event.marshaller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.ElementAttribute;
import nostr.base.IMarshaller;
import nostr.base.Relay;
import nostr.event.impl.OtsEvent;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@AllArgsConstructor
@Data
@Builder
public class OstEventMarshaller implements IMarshaller {

    private final OtsEvent event;
    private final Relay relay;

    @Override
    public String marshall() throws NostrException {
        return toJson();
    }
    
    private String toJson() throws NostrException {
        try {
            JsonNode node = MAPPER.valueToTree(event);
            ObjectNode objNode = (ObjectNode) node;
            event.getAttributes().parallelStream()
                    .map(ElementAttribute::getValue)
                    .forEach(ev -> {
                        var expression = (HashMap<String, String>) ev;

                        objNode.set("ots", MAPPER.valueToTree(expression.get("ots")));
                    });

            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new NostrException(e);
        }
    }

}
