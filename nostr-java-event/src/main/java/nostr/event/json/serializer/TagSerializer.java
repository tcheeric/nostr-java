package nostr.event.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import nostr.base.ElementAttribute;
import nostr.event.BaseTag;
import nostr.event.impl.GenericTag;
import nostr.util.NostrException;
import org.apache.commons.lang3.stream.Streams;

import java.io.IOException;
import java.io.Serial;
import java.util.List;

import static nostr.event.json.codec.BaseTagEncoder.BASETAG_ENCODER_MAPPED_AFTERBURNER;

public class TagSerializer extends StdSerializer<BaseTag> {

	@Serial
	private static final long serialVersionUID = -3877972991082754068L;

	public TagSerializer() {
		super(BaseTag.class);
	}

	@Override
	public void serialize(BaseTag value, JsonGenerator gen, SerializerProvider serializers) {
		try {
			final ObjectNode node = BASETAG_ENCODER_MAPPED_AFTERBURNER.getNodeFactory().objectNode();
			Streams.failableStream(value.getSupportedFields().stream()).forEach(f -> node.put(f.getName(), value.getFieldValue(f)));

			if (value instanceof GenericTag genericTag) {
				List<ElementAttribute> attrs = genericTag.getAttributes();
				attrs.forEach(a -> node.put(a.getName(), a.getValue().toString()));
			}

			ArrayNode arrayNode = node.objectNode().putArray("values").add(value.getCode());
			node.fields().forEachRemaining(entry -> arrayNode.add(entry.getValue().asText()));
			gen.writePOJO(arrayNode);
		} catch (IOException | NostrException e) {
			throw new RuntimeException(e);
		}
	}
}
