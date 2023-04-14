package nostr.event.marshaller.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import nostr.base.ElementAttribute;
import nostr.base.IMarshaller;
import nostr.base.Relay;
import nostr.event.impl.OtsEvent;
import nostr.types.values.impl.ExpressionValue;
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
    
    @Override
    public String toJson() throws NostrException {
    	try {
	    	JsonNode node = MAPPER.valueToTree(event);
	    	ObjectNode objNode = (ObjectNode) node;
    		event.getAttributes().parallelStream()
    			.map(ElementAttribute::getValue)
    			.forEach(ev -> {
    				var expression = (ExpressionValue) ev;
    				
    		    	objNode.set(expression.getName(), MAPPER.valueToTree(expression.getValue().toString()));
    			});
    		
	    	return MAPPER.writeValueAsString(node);
		} catch (Exception e) {
			throw new NostrException(e);
		} 
    }

}
