package nostr.event.serializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.extern.java.Log;
import nostr.base.IMarshaller;
import nostr.base.ITag;

/**
 * @author guilhermegps
 *
 */
@Log
public class CustomTagSerializer extends StdSerializer<ITag> {

	private static final long serialVersionUID = -3877972991082754068L;

	public CustomTagSerializer() {
		super(ITag.class);
	}

	@Override
	public void serialize(ITag value, JsonGenerator gen, SerializerProvider serializers) {
		try {
		    var mapper = IMarshaller.MAPPER;
	    	JsonNode node = mapper.valueToTree(value);
	    	
	    	Iterator<Entry<String,JsonNode>> fields = node.fields();
	    	var list = StreamSupport.stream(
	                Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false)
	                .map(f -> f.getValue().asText().toLowerCase() )
	                .collect(Collectors.toList());
			
			gen.writePOJO(list);
		} catch (IOException e) {
            log.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
		}
	}

}