package nostr.event.json.serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import nostr.event.tag.GenericTag;
import java.io.Serial;

public class GenericTagSerializer<T extends GenericTag> extends AbstractTagSerializer<T> {

	@Serial
	private static final long serialVersionUID = -5318614324350049034L;

	public GenericTagSerializer() {
		super((Class<T>) GenericTag.class);
	}

	@Override
	protected void processNode(ObjectNode node, T value) {
		value.getAttributes().forEach(a -> node.put(a.getName(), a.getValue().toString()));
	}
}
