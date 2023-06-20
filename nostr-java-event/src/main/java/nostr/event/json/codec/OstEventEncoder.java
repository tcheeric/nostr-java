package nostr.event.json.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nostr.base.ElementAttribute;
import static nostr.base.IEncoder.MAPPER;
import nostr.base.Relay;
import nostr.event.impl.OtsEvent;
import nostr.util.NostrException;

/**
 * @author guilhermegps
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OstEventEncoder extends ElementEncoder {

    public OstEventEncoder(OtsEvent event, Relay relay) {
        super(event, relay);
    }

    public OstEventEncoder(OtsEvent event) {
        super(event);
    }

    @Override
    public String encode() throws NostrException {
        return toJson();
    }

    @Override
    protected String toJson() throws NostrException {
        try {
            final OtsEvent event = (OtsEvent) getElement();
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
