package nostr.event.serializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import lombok.extern.java.Log;
import nostr.event.list.BaseList;


/**
 * @author guilhermegps
 *
 */
@Log
public class CustomBaseListSerializer extends JsonSerializer<BaseList> {

	@Override
	public void serialize(BaseList value, JsonGenerator gen, SerializerProvider serializers) {
		try {
			var list = value.getList().parallelStream().map(obj -> toJson(obj))
					.collect(Collectors.toList());
			
			gen.writePOJO(list);
		} catch (IOException e) {
            log.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
		}
	}
    
    protected JsonNode toJson(Object obj) {
	    var mapper = new ObjectMapper()
	    		.setSerializationInclusion(Include.NON_NULL);
    	try {
	    	JsonNode node = mapper.valueToTree(obj);
	    	
	    	if(node.isObject()) {
		    	Iterator<Entry<String,JsonNode>> fields = node.fields();
		    	
		    	var list = StreamSupport.stream(
		                Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED), false)
		                .map(f -> f.getValue().asText().toLowerCase() )
		                .collect(Collectors.toList());
		    	
		    	return mapper.valueToTree(list);
	    	}
	    	
	    	return node;
		} catch (Exception e) {
            log.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
		} 
    }

}
