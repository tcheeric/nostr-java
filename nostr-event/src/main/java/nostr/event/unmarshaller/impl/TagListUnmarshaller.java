package nostr.event.unmarshaller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import nostr.base.IMarshaller;
import nostr.base.IUnmarshaller;
import nostr.event.list.TagList;

/**
 *
 * @author squirrel
 */
@Data
@AllArgsConstructor
public class TagListUnmarshaller implements IUnmarshaller<TagList> {

    private final String json;
    private final boolean escape;

    public TagListUnmarshaller(String tagList) {
        this(tagList, false);
    }

    @Override
    public TagList unmarshall() {
    	var mapper = IMarshaller.MAPPER;
    	TagList result = new TagList();
    	try {
			var arrayNode = (ArrayNode) mapper.readTree(this.getJson());
			
			for (JsonNode node : arrayNode) {
				result.add(new TagUnmarshaller(node.toString()).unmarshall());
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	
    	return result;
    }

}
