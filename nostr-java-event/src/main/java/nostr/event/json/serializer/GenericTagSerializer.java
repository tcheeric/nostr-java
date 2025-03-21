package nostr.event.json.serializer;

import nostr.base.ElementAttribute;
import nostr.event.tag.GenericTag;

import java.io.Serial;
import java.util.List;

public class GenericTagSerializer<T extends GenericTag> extends AbstractTagSerializer<T> {

	@Serial
	private static final long serialVersionUID = -5318614324350049034L;

	public GenericTagSerializer() {
		super((Class<T>) GenericTag.class);
	}

	@Override
	public void processNode(T value) {
		List<ElementAttribute> attrs = genericTag.getAttributes();
		attrs.forEach(a -> node.put(a.getName(), a.getValue().toString()));
	}
}
